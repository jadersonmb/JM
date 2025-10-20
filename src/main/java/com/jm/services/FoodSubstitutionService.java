package com.jm.services;

import com.jm.dto.food.FoodSubstitutionQueryDTO;
import com.jm.dto.food.FoodSubstitutionRequestDTO;
import com.jm.dto.food.FoodSubstitutionResponseDTO;
import com.jm.dto.food.FoodSubstitutionSuggestionDTO;
import com.jm.dto.food.FoodSuggestionDTO;
import com.jm.entity.Food;
import com.jm.entity.MeasurementUnits;
import com.jm.repository.FoodRepository;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class FoodSubstitutionService {

    private static final int DEFAULT_LIMIT = 3;

    private final FoodRepository foodRepository;

    public FoodSubstitutionService(FoodRepository foodRepository) {
        this.foodRepository = foodRepository;
    }

    @Transactional(readOnly = true)
    public FoodSubstitutionResponseDTO generateSuggestions(FoodSubstitutionRequestDTO request) {
        List<Food> activeFoods = foodRepository.findByIsActiveTrueOrderByNameAsc();
        Map<UUID, NormalizedFood> normalizedFoods = activeFoods.stream()
                .map(this::normalizeFood)
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(NormalizedFood::id, food -> food, (left, right) -> left, ConcurrentHashMap::new));

        List<FoodSubstitutionSuggestionDTO> suggestions = new ArrayList<>();

        if (request == null || request.getItems() == null || request.getItems().isEmpty()) {
            return FoodSubstitutionResponseDTO.builder().suggestions(suggestions).build();
        }

        for (FoodSubstitutionQueryDTO query : request.getItems()) {
            if (query == null || query.getFoodId() == null) {
                continue;
            }
            NormalizedFood original = normalizedFoods.get(query.getFoodId());
            if (original == null) {
                continue;
            }
            List<FoodSuggestionDTO> alternatives = selectAlternatives(original, normalizedFoods, resolveLimit(query));
            FoodSubstitutionSuggestionDTO suggestion = FoodSubstitutionSuggestionDTO.builder()
                    .reference(query.getReference())
                    .original(toSuggestionDTO(original))
                    .alternatives(alternatives)
                    .build();
            suggestions.add(suggestion);
        }

        return FoodSubstitutionResponseDTO.builder().suggestions(suggestions).build();
    }

    private int resolveLimit(FoodSubstitutionQueryDTO query) {
        if (query == null || query.getLimit() == null || query.getLimit() <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(query.getLimit(), 10);
    }

    private List<FoodSuggestionDTO> selectAlternatives(NormalizedFood original, Map<UUID, NormalizedFood> normalizedFoods,
            int limit) {
        List<Candidate> candidates = normalizedFoods.values().stream()
                .filter(candidate -> !candidate.id().equals(original.id()))
                .map(candidate -> new Candidate(candidate, computeSimilarity(original, candidate)))
                .collect(Collectors.toList());

        List<Candidate> primaryMatches = candidates.stream()
                .filter(candidate -> Objects.equals(candidate.food().primary(), original.primary()))
                .sorted(Comparator.comparingDouble(Candidate::score))
                .collect(Collectors.toList());

        List<Candidate> fallbackMatches = candidates.stream()
                .filter(candidate -> !Objects.equals(candidate.food().primary(), original.primary()))
                .sorted(Comparator.comparingDouble(Candidate::score))
                .collect(Collectors.toList());

        return concat(primaryMatches, fallbackMatches).stream()
                .limit(limit)
                .map(candidate -> toSuggestionDTO(candidate.food()))
                .collect(Collectors.toList());
    }

    private List<Candidate> concat(List<Candidate> left, List<Candidate> right) {
        List<Candidate> combined = new ArrayList<>(left.size() + right.size());
        combined.addAll(left);
        combined.addAll(right);
        return combined;
    }

    private FoodSuggestionDTO toSuggestionDTO(NormalizedFood food) {
        if (food == null) {
            return null;
        }
        return FoodSuggestionDTO.builder()
                .id(food.id())
                .name(food.name())
                .categoryName(food.categoryName())
                .calories(food.calories())
                .protein(food.protein())
                .carbs(food.carbs())
                .fat(food.fat())
                .fiber(food.fiber())
                .portion(food.portion())
                .portionUnit(food.portionUnit())
                .portionLabel(food.portionLabel())
                .primary(food.primary())
                .build();
    }

    private NormalizedFood normalizeFood(Food entity) {
        if (entity == null || entity.getId() == null) {
            return null;
        }
        double protein = toNumber(entity.getAverageProtein());
        double carbs = toNumber(entity.getAverageCarbs());
        double fat = toNumber(entity.getAverageFat());
        double calories = toNumber(entity.getAverageCalories());
        String categoryName = entity.getFoodCategory() != null ? entity.getFoodCategory().getName() : "";
        double fiber = deriveFiber(carbs, categoryName);
        String primary = determinePrimaryNutrient(protein, carbs, fat, fiber, categoryName);
        BigDecimal portionValue = entity.getCommonPortion();
        Double portion = portionValue != null ? portionValue.doubleValue() : null;
        String portionUnit = resolveUnitLabel(entity.getCommonPortionUnit());
        String portionLabel = formatPortionLabel(portionValue, portionUnit);
        return new NormalizedFood(entity.getId(), entity.getName(), categoryName, calories, protein, carbs, fat, fiber, portion,
                portionUnit, portionLabel, primary);
    }

    private double toNumber(BigDecimal value) {
        return value != null ? value.doubleValue() : 0d;
    }

    private double computeSimilarity(NormalizedFood source, NormalizedFood candidate) {
        double score = 0d;
        score += Math.abs(source.protein() - candidate.protein());
        score += Math.abs(source.carbs() - candidate.carbs());
        score += Math.abs(source.fat() - candidate.fat());
        score += Math.abs(source.fiber() - candidate.fiber());
        score += Math.abs(source.calories() - candidate.calories()) / 50d;
        return score;
    }

    private double deriveFiber(double carbs, String categoryName) {
        String normalizedCategory = categoryName != null ? categoryName.toLowerCase(Locale.ROOT) : "";
        if (carbs <= 0) {
            if (normalizedCategory.contains("legume") || normalizedCategory.contains("vegetable")
                    || normalizedCategory.contains("fruit")) {
                return 1d;
            }
            return 0d;
        }
        if (normalizedCategory.contains("legume")) {
            return roundToSingleDecimal(Math.max(carbs * 0.30d, 2d));
        }
        if (normalizedCategory.contains("vegetable") || normalizedCategory.contains("fruit")) {
            return roundToSingleDecimal(Math.max(carbs * 0.25d, 1d));
        }
        if (normalizedCategory.contains("grain") || normalizedCategory.contains("bread")) {
            return roundToSingleDecimal(Math.max(carbs * 0.15d, 1d));
        }
        return 0d;
    }

    private double roundToSingleDecimal(double value) {
        return Math.round(value * 10d) / 10d;
    }

    private String determinePrimaryNutrient(double protein, double carbs, double fat, double fiber, String categoryName) {
        double primaryValue = protein;
        String primary = "PROTEIN";
        if (carbs > primaryValue) {
            primaryValue = carbs;
            primary = "CARBS";
        }
        if (fat > primaryValue) {
            primaryValue = fat;
            primary = "FAT";
        }
        if (fiber > primaryValue) {
            primaryValue = fiber;
            primary = "FIBER";
        }
        if (primaryValue > 0) {
            return primary;
        }
        return mapCategoryToNutrient(categoryName);
    }

    private String mapCategoryToNutrient(String categoryName) {
        String normalized = categoryName != null ? categoryName.toLowerCase(Locale.ROOT) : "";
        if (normalized.contains("protein") || normalized.contains("dairy")) {
            return "PROTEIN";
        }
        if (normalized.contains("fat") || normalized.contains("oil") || normalized.contains("nut")
                || normalized.contains("seed")) {
            return "FAT";
        }
        if (normalized.contains("vegetable") || normalized.contains("fruit") || normalized.contains("legume")) {
            return "FIBER";
        }
        if (normalized.contains("grain") || normalized.contains("bread") || normalized.contains("sweet")) {
            return "CARBS";
        }
        return "CARBS";
    }

    private String resolveUnitLabel(MeasurementUnits unit) {
        if (unit == null) {
            return null;
        }
        if (StringUtils.hasText(unit.getDescription())) {
            return unit.getDescription();
        }
        if (StringUtils.hasText(unit.getSymbol())) {
            return unit.getSymbol();
        }
        return null;
    }

    private String formatPortionLabel(BigDecimal portion, String unitName) {
        if (portion == null) {
            return unitName;
        }
        double value = portion.doubleValue();
        if (value <= 0d) {
            return unitName;
        }
        boolean hasDecimals = Math.abs(value % 1d) > 0d;
        NumberFormat formatter = NumberFormat.getNumberInstance();
        formatter.setMinimumFractionDigits(hasDecimals ? 1 : 0);
        formatter.setMaximumFractionDigits(hasDecimals ? 1 : 0);
        String formatted = formatter.format(value);
        if (!StringUtils.hasText(unitName)) {
            return formatted;
        }
        return formatted + " " + unitName;
    }

    private record NormalizedFood(UUID id, String name, String categoryName, double calories, double protein, double carbs,
            double fat, double fiber, Double portion, String portionUnit, String portionLabel, String primary) {
    }

    private record Candidate(NormalizedFood food, double score) {
    }
}
