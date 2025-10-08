package com.jm.services;

import com.jm.dto.FoodDTO;
import com.jm.entity.Food;
import com.jm.repository.FoodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FoodService {

    private final FoodRepository repository;

    public List<FoodDTO> findAll(String language, UUID categoryId) {
        Map<UUID, Food> accumulator = new LinkedHashMap<>();
        if (StringUtils.hasText(language)) {
            if (categoryId != null) {
                repository.findByFoodCategoryIdAndLanguageIgnoreCaseOrderByNameAsc(categoryId, language)
                        .forEach(food -> accumulator.put(food.getId(), food));
                repository.findByFoodCategoryIdAndLanguageIsNullOrderByNameAsc(categoryId)
                        .forEach(food -> accumulator.putIfAbsent(food.getId(), food));
            } else {
                repository.findByLanguageIgnoreCaseOrderByNameAsc(language)
                        .forEach(food -> accumulator.put(food.getId(), food));
                repository.findByLanguageIsNullOrderByNameAsc()
                        .forEach(food -> accumulator.putIfAbsent(food.getId(), food));
            }
        } else {
            if (categoryId != null) {
                repository.findByFoodCategoryIdAndLanguageIsNullOrderByNameAsc(categoryId)
                        .forEach(food -> accumulator.put(food.getId(), food));
            } else {
                repository.findAllByOrderByNameAsc()
                        .forEach(food -> accumulator.put(food.getId(), food));
            }
        }
        return accumulator.values().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private FoodDTO toDto(Food entity) {
        return FoodDTO.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .name(entity.getName())
                .description(entity.getDescription())
                .categoryId(entity.getFoodCategory() != null ? entity.getFoodCategory().getId() : null)
                .categoryName(entity.getFoodCategory() != null ? entity.getFoodCategory().getName() : null)
                .averageCalories(entity.getAverageCalories())
                .averageProtein(entity.getAverageProtein())
                .averageCarbs(entity.getAverageCarbs())
                .averageFat(entity.getAverageFat())
                .commonPortion(entity.getCommonPortion())
                .commonPortionUnitId(
                        entity.getCommonPortionUnit() != null ? entity.getCommonPortionUnit().getId() : null)
                .commonPortionUnitName(
                        entity.getCommonPortionUnit() != null ? entity.getCommonPortionUnit().getDescription() : null)
                .build();
    }
}
