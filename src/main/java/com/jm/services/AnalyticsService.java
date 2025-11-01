package com.jm.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jm.dto.analytics.BodyCompositionPointDTO;
import com.jm.dto.analytics.BodyCompositionResponseDTO;
import com.jm.dto.analytics.GoalAdherenceMetricDTO;
import com.jm.dto.analytics.GoalAdherenceResponseDTO;
import com.jm.dto.analytics.HydrationEntryDTO;
import com.jm.dto.analytics.HydrationResponseDTO;
import com.jm.dto.analytics.MacroDistributionEntryDTO;
import com.jm.dto.analytics.MacroDistributionResponseDTO;
import com.jm.dto.analytics.TopFoodDTO;
import com.jm.dto.analytics.TopFoodsResponseDTO;
import com.jm.entity.Anamnesis;
import com.jm.entity.MeasurementUnits;
import com.jm.entity.NutritionAnalysis;
import com.jm.entity.NutritionGoal;
import com.jm.entity.Users;
import com.jm.entity.WhatsAppMessage;
import com.jm.enums.AnalyticsGroupBy;
import com.jm.enums.NutritionGoalPeriodicity;
import com.jm.enums.NutritionGoalTargetMode;
import com.jm.enums.NutritionGoalType;
import com.jm.execption.JMException;
import com.jm.execption.ProblemType;
import com.jm.repository.AnamnesisRepository;
import com.jm.repository.NutritionAnalysisRepository;
import com.jm.repository.NutritionGoalRepository;
import com.jm.repository.UserRepository;
import com.jm.utils.SecurityUtils;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AnalyticsService {

    private static final int DEFAULT_RANGE_DAYS = 7;
    private static final int MAX_RANGE_DAYS = 365;
    private static final MathContext MATH_CONTEXT = MathContext.DECIMAL64;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    private static final Pattern NUMBER_PATTERN = Pattern.compile("(\\d+(?:[\\.,]\\d+)?)");
    private static final Pattern MASS_PATTERN = Pattern
            .compile("(\\d+(?:[\\.,]\\d+)?)\\s*(kg|kilograms?|g|grams?|gramas?)", Pattern.CASE_INSENSITIVE);
    private static final Pattern VOLUME_PATTERN = Pattern.compile("(\\d+(?:[\\.,]\\d+)?)\\s*(ml|l|lt|litro?s?)",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern FIBER_PATTERN = Pattern.compile("(?:fiber|fibra)\\s*:?\\s*(\\d+(?:[\\.,]\\d+)?)\\s*g",
            Pattern.CASE_INSENSITIVE);

    private static final BigDecimal ONE_THOUSAND = BigDecimal.valueOf(1000);
    private static final BigDecimal SEVEN = BigDecimal.valueOf(7);
    private static final BigDecimal THIRTY = BigDecimal.valueOf(30);

    private static final Map<NutritionGoalType, String> GOAL_TYPE_TO_METRIC = Map.ofEntries(
            Map.entry(NutritionGoalType.PROTEIN, "protein"),
            Map.entry(NutritionGoalType.CARBOHYDRATE, "carbs"),
            Map.entry(NutritionGoalType.FAT, "fat"),
            Map.entry(NutritionGoalType.WATER, "water"),
            Map.entry(NutritionGoalType.FIBER, "fiber"),
            Map.entry(NutritionGoalType.ENERGY, "calories"),
            Map.entry(NutritionGoalType.CALORIE_TARGET, "calories"));

    private static final List<String> METRIC_ORDER = List.of("protein", "carbs", "fat", "water", "fiber", "calories");

    private final NutritionGoalRepository nutritionGoalRepository;
    private final NutritionAnalysisRepository nutritionAnalysisRepository;
    private final AnamnesisRepository anamnesisRepository;
    private final UserRepository userRepository;
    private final MessageSource messageSource;
    private final ObjectMapper objectMapper;

    public AnalyticsService(NutritionGoalRepository nutritionGoalRepository,
            NutritionAnalysisRepository nutritionAnalysisRepository,
            AnamnesisRepository anamnesisRepository,
            UserRepository userRepository,
            MessageSource messageSource,
            ObjectMapper objectMapper) {
        this.nutritionGoalRepository = nutritionGoalRepository;
        this.nutritionAnalysisRepository = nutritionAnalysisRepository;
        this.anamnesisRepository = anamnesisRepository;
        this.userRepository = userRepository;
        this.messageSource = messageSource;
        this.objectMapper = objectMapper;
    }

    public GoalAdherenceResponseDTO getGoalsAdherence(Integer range, String groupByValue, UUID userId) {
        AnalyticsContext context = buildContext(range, groupByValue, userId);
        List<NutritionGoal> goals = loadGoals(context);
        AnalysisSnapshot snapshot = analyze(context);

        Map<String, BigDecimal> targets = computeTargets(context, goals);
        Map<String, BigDecimal> achieved = computeAchievements(snapshot);

        List<GoalAdherenceMetricDTO> metrics = METRIC_ORDER.stream()
                .map(metric -> {
                    BigDecimal target = scale(targets.getOrDefault(metric, BigDecimal.ZERO));
                    BigDecimal actual = scale(achieved.getOrDefault(metric, BigDecimal.ZERO));
                    BigDecimal percent = target.compareTo(BigDecimal.ZERO) == 0
                            ? BigDecimal.ZERO
                            : actual.divide(target, MATH_CONTEXT).multiply(BigDecimal.valueOf(100)).setScale(1,
                                    ROUNDING_MODE);
                    return GoalAdherenceMetricDTO.builder()
                            .key(metric)
                            .target(target)
                            .achieved(actual)
                            .percent(percent)
                            .build();
                })
                .collect(Collectors.toList());

        return GoalAdherenceResponseDTO.builder()
                .startDate(context.startDate())
                .endDate(context.endDate())
                .metrics(metrics)
                .build();
    }

    public MacroDistributionResponseDTO getMacroDistribution(Integer range, String groupByValue, UUID userId) {
        AnalyticsContext context = buildContext(range, groupByValue, userId);
        AnalysisSnapshot snapshot = analyze(context);

        List<MacroDistributionEntryDTO> entries = snapshot.buckets().stream()
                .map(bucket -> {
                    MacroTotals totals = snapshot.macroByBucket().get(bucket);
                    return MacroDistributionEntryDTO.builder()
                            .label(bucket.label(context.groupBy()))
                            .protein(scale(totals.protein()))
                            .carbs(scale(totals.carbs()))
                            .fat(scale(totals.fat()))
                            .calories(scale(totals.calories()))
                            .build();
                })
                .collect(Collectors.toList());

        return MacroDistributionResponseDTO.builder()
                .startDate(context.startDate())
                .endDate(context.endDate())
                .groupBy(context.groupBy().name().toLowerCase(Locale.ROOT))
                .series(entries)
                .build();
    }

    public HydrationResponseDTO getHydration(Integer range, String groupByValue, UUID userId) {
        AnalyticsContext context = buildContext(range, groupByValue, userId);
        List<NutritionGoal> goals = loadGoals(context);
        AnalysisSnapshot snapshot = analyze(context);
        BigDecimal dailyTarget = computeDailyWaterTarget(context, goals);

        List<HydrationEntryDTO> entries = snapshot.buckets().stream()
                .map(bucket -> {
                    BigDecimal intake = snapshot.hydrationByBucket().get(bucket);
                    BigDecimal target = dailyTarget.multiply(BigDecimal.valueOf(bucket.activeDays()));
                    return HydrationEntryDTO.builder()
                            .label(bucket.label(context.groupBy()))
                            .intake(scale(intake))
                            .target(scale(target))
                            .build();
                })
                .collect(Collectors.toList());

        return HydrationResponseDTO.builder()
                .startDate(context.startDate())
                .endDate(context.endDate())
                .groupBy(context.groupBy().name().toLowerCase(Locale.ROOT))
                .series(entries)
                .build();
    }

    public TopFoodsResponseDTO getTopFoods(Integer range, String groupByValue, UUID userId) {
        AnalyticsContext context = buildContext(range, groupByValue, userId);
        AnalysisSnapshot snapshot = analyze(context);

        List<TopFoodDTO> items = snapshot.foods().values().stream()
                .sorted(Comparator.comparing(FoodAggregate::quantity).reversed())
                .limit(10)
                .map(food -> TopFoodDTO.builder()
                        .name(food.name())
                        .unit(food.unit())
                        .quantity(scale(food.quantity()))
                        .build())
                .collect(Collectors.toList());

        return TopFoodsResponseDTO.builder()
                .startDate(context.startDate())
                .endDate(context.endDate())
                .items(items)
                .build();
    }

    public BodyCompositionResponseDTO getBodyComposition(Integer range, String groupByValue, UUID userId) {
        AnalyticsContext context = buildContext(range, groupByValue, userId);
        List<Anamnesis> anamneses = loadAnamneses(context);
        if (anamneses.isEmpty()) {
            return BodyCompositionResponseDTO.builder()
                    .startDate(context.startDate())
                    .endDate(context.endDate())
                    .series(Collections.emptyList())
                    .build();
        }

        Map<LocalDate, BodyAggregate> aggregates = new TreeMap<>();
        for (Anamnesis anamnesis : anamneses) {
            LocalDate date = LocalDate.from(anamnesis.getCreatedAt());
            BodyAggregate aggregate = aggregates.computeIfAbsent(date, key -> new BodyAggregate());
            aggregate.add(anamnesis.getWeightKg(), anamnesis.getBodyMassIndex(),
                    anamnesis.getBodyFatPercentage(), anamnesis.getMuscleMassPercentage());
        }

        List<BodyCompositionPointDTO> points = aggregates.entrySet().stream()
                .map(entry -> BodyCompositionPointDTO.builder()
                        .label(entry.getKey().toString())
                        .weight(scale(entry.getValue().weightAvg()))
                        .bmi(scale(entry.getValue().bmiAvg()))
                        .fatPercentage(scale(entry.getValue().fatAvg()))
                        .musclePercentage(scale(entry.getValue().muscleAvg()))
                        .build())
                .collect(Collectors.toList());

        return BodyCompositionResponseDTO.builder()
                .startDate(context.startDate())
                .endDate(context.endDate())
                .series(points)
                .build();
    }

    private AnalyticsContext buildContext(Integer range, String groupByValue, UUID requestedUserId) {
        int rangeDays = range == null ? DEFAULT_RANGE_DAYS : Math.max(1, Math.min(range, MAX_RANGE_DAYS));
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(rangeDays - 1L);
        AnalyticsGroupBy groupBy = AnalyticsGroupBy.from(groupByValue);
        ZoneId zoneId = ZoneId.systemDefault();

        OffsetDateTime startDateTime = startDate.atStartOfDay(zoneId).toOffsetDateTime();
        OffsetDateTime endDateTime = endDate.plusDays(1).atStartOfDay(zoneId).toOffsetDateTime();

        boolean isClient = SecurityUtils.hasRole("CLIENT");
        Set<UUID> userIds = new LinkedHashSet<>();
        Set<String> phones = new LinkedHashSet<>();
        boolean includeAll = false;

        if (isClient) {
            UUID currentUserId = SecurityUtils.getCurrentUserId().orElseThrow(this::forbidden);
            if (requestedUserId != null && !requestedUserId.equals(currentUserId)) {
                throw forbidden();
            }
            userIds.add(currentUserId);
            userRepository.findById(currentUserId)
                    .map(Users::getPhoneNumber)
                    .map(this::normalizePhone)
                    .filter(StringUtils::hasText)
                    .ifPresent(phones::add);
        } else if (requestedUserId != null) {
            Users user = userRepository.findById(requestedUserId).orElseThrow(this::userNotFound);
            userIds.add(user.getId());
            Optional.ofNullable(user.getPhoneNumber())
                    .map(this::normalizePhone)
                    .filter(StringUtils::hasText)
                    .ifPresent(phones::add);
        } else {
            includeAll = true;
        }

        return new AnalyticsContext(
                startDate,
                endDate,
                rangeDays,
                groupBy,
                startDateTime,
                endDateTime,
                zoneId,
                includeAll,
                Collections.unmodifiableSet(userIds),
                Collections.unmodifiableSet(phones),
                new ConcurrentHashMap<>(),
                new ConcurrentHashMap<>());
    }

    private List<NutritionGoal> loadGoals(AnalyticsContext context) {
        if (!context.userIds().isEmpty()) {
            if (context.userIds().size() == 1) {
                UUID userId = context.userIds().iterator().next();
                return nutritionGoalRepository.findByCreatedById(userId);
            }
            return nutritionGoalRepository.findByCreatedByIdIn(context.userIds());
        }
        return nutritionGoalRepository.findByActiveTrue();
    }

    private List<Anamnesis> loadAnamneses(AnalyticsContext context) {
        if (context.includeAllUsers()) {
            return anamnesisRepository.findAll();
        }
        if (context.userIds().isEmpty()) {
            return Collections.emptyList();
        }
        return context.userIds().stream()
                .map(anamnesisRepository::findByUserIdOrderByIdAsc)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    private AnalysisSnapshot analyze(AnalyticsContext context) {
        List<Bucket> buckets = buildBuckets(context);
        Map<Bucket, MacroTotals> macroByBucket = new LinkedHashMap<>();
        Map<Bucket, BigDecimal> hydration = new LinkedHashMap<>();
        Map<Bucket, BigDecimal> fiber = new LinkedHashMap<>();
        buckets.forEach(bucket -> {
            macroByBucket.put(bucket, new MacroTotals());
            hydration.put(bucket, BigDecimal.ZERO);
            fiber.put(bucket, BigDecimal.ZERO);
        });

        MacroTotals totalMacros = new MacroTotals();
        BigDecimal totalWater = BigDecimal.ZERO;
        BigDecimal totalFiber = BigDecimal.ZERO;
        Map<String, FoodAggregate> foods = new LinkedHashMap<>();

        List<NutritionAnalysis> analyses = loadAnalyses(context);
        for (NutritionAnalysis analysis : analyses) {
            OffsetDateTime createdAt = analysis.getCreatedAt();
            if (createdAt == null) {
                continue;
            }
            LocalDate date = createdAt.atZoneSameInstant(context.zoneId()).toLocalDate();
            Bucket bucket = findBucket(buckets, date);
            if (bucket == null) {
                continue;
            }

            MacroTotals bucketTotals = macroByBucket.get(bucket);
            bucketTotals.add(analysis);
            totalMacros.add(analysis);

            BigDecimal water = estimateWater(analysis);
            if (water.signum() > 0) {
                hydration.put(bucket, hydration.get(bucket).add(water));
                totalWater = totalWater.add(water);
            }

            BigDecimal fiberValue = estimateFiber(analysis);
            if (fiberValue.signum() > 0) {
                fiber.put(bucket, fiber.get(bucket).add(fiberValue));
                totalFiber = totalFiber.add(fiberValue);
            }

            FoodAggregate food = estimateFood(analysis);
            if (food != null) {
                String key = food.name().toLowerCase(Locale.ROOT) + "|" + food.unit();
                foods.compute(key, (k, existing) -> {
                    if (existing == null) {
                        return food;
                    }
                    existing.add(food.quantity());
                    return existing;
                });
            }
        }

        return new AnalysisSnapshot(buckets, macroByBucket, totalMacros, hydration, totalWater, fiber, totalFiber,
                foods);
    }

    private List<NutritionAnalysis> loadAnalyses(AnalyticsContext context) {
        List<NutritionAnalysis> analyses = nutritionAnalysisRepository.findByCreatedAtBetween(
                context.startDateTime(), context.endDateTime());
        if (context.phones().isEmpty()) {
            return analyses;
        }
        return analyses.stream()
                .filter(analysis -> {
                    WhatsAppMessage message = analysis.getMessage();
                    if (message == null || !StringUtils.hasText(message.getFromPhone())) {
                        return false;
                    }
                    String normalized = normalizePhone(message.getFromPhone());
                    return StringUtils.hasText(normalized) && context.phones().contains(normalized);
                })
                .collect(Collectors.toList());
    }

    private Map<String, BigDecimal> computeTargets(AnalyticsContext context, List<NutritionGoal> goals) {
        Map<String, BigDecimal> targets = new LinkedHashMap<>();
        METRIC_ORDER.forEach(metric -> targets.put(metric, BigDecimal.ZERO));

        for (NutritionGoal goal : goals) {
            if (goal == null || Boolean.FALSE.equals(goal.getActive())) {
                continue;
            }
            UUID ownerId = goal.getCreatedBy() != null ? goal.getCreatedBy().getId() : null;
            if (!isUserAllowed(context, ownerId) || !isGoalInRange(goal, context)) {
                continue;
            }
            String metric = GOAL_TYPE_TO_METRIC.get(goal.getType());
            if (!StringUtils.hasText(metric)) {
                continue;
            }
            BigDecimal weight = resolveWeight(ownerId, context);
            BigDecimal dailyTarget = resolveDailyTarget(goal, weight, context);
            if (dailyTarget.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            int activeDays = overlapDays(goal, context);
            if (activeDays <= 0) {
                continue;
            }
            BigDecimal contribution = dailyTarget.multiply(BigDecimal.valueOf(activeDays), MATH_CONTEXT);
            targets.merge(metric, contribution, BigDecimal::add);
        }

        if (targets.get("water").compareTo(BigDecimal.ZERO) == 0) {
            BigDecimal fallback = fallbackWaterTarget(context);
            if (fallback.compareTo(BigDecimal.ZERO) > 0) {
                targets.put("water", fallback.multiply(BigDecimal.valueOf(context.rangeDays()), MATH_CONTEXT));
            }
        }

        return targets;
    }

    private Map<String, BigDecimal> computeAchievements(AnalysisSnapshot snapshot) {
        Map<String, BigDecimal> achieved = new LinkedHashMap<>();
        achieved.put("protein", snapshot.totalMacros().protein());
        achieved.put("carbs", snapshot.totalMacros().carbs());
        achieved.put("fat", snapshot.totalMacros().fat());
        achieved.put("calories", snapshot.totalMacros().calories());
        achieved.put("water", snapshot.totalWater());
        achieved.put("fiber", snapshot.totalFiber());
        return achieved;
    }

    private BigDecimal computeDailyWaterTarget(AnalyticsContext context, List<NutritionGoal> goals) {
        Map<UUID, BigDecimal> perUser = new LinkedHashMap<>();
        for (NutritionGoal goal : goals) {
            if (goal == null || Boolean.FALSE.equals(goal.getActive()) || goal.getType() != NutritionGoalType.WATER) {
                continue;
            }
            UUID ownerId = goal.getCreatedBy() != null ? goal.getCreatedBy().getId() : null;
            if (!isUserAllowed(context, ownerId) || !isGoalInRange(goal, context)) {
                continue;
            }
            BigDecimal weight = resolveWeight(ownerId, context);
            BigDecimal daily = resolveDailyTarget(goal, weight, context);
            if (daily.compareTo(BigDecimal.ZERO) > 0) {
                perUser.merge(ownerId, daily, BigDecimal::add);
            }
        }
        ensureWaterFallback(context, perUser);
        return perUser.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal fallbackWaterTarget(AnalyticsContext context) {
        Map<UUID, BigDecimal> perUser = new LinkedHashMap<>();
        ensureWaterFallback(context, perUser);
        return perUser.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void ensureWaterFallback(AnalyticsContext context, Map<UUID, BigDecimal> perUser) {
        if (context.includeAllUsers()) {
            return;
        }
        for (UUID userId : context.userIds()) {
            perUser.computeIfAbsent(userId, id -> resolveWaterIntake(id, context));
        }
    }

    private boolean isGoalInRange(NutritionGoal goal, AnalyticsContext context) {
        LocalDate start = goal.getStartDate();
        LocalDate end = goal.getEndDate();
        if (start != null && start.isAfter(context.endDate())) {
            return false;
        }
        if (end != null && end.isBefore(context.startDate())) {
            return false;
        }
        return true;
    }

    private int overlapDays(NutritionGoal goal, AnalyticsContext context) {
        LocalDate effectiveStart = goal.getStartDate() != null ? goal.getStartDate() : context.startDate();
        LocalDate effectiveEnd = goal.getEndDate() != null ? goal.getEndDate() : context.endDate();
        LocalDate start = effectiveStart.isBefore(context.startDate()) ? context.startDate() : effectiveStart;
        LocalDate end = effectiveEnd.isAfter(context.endDate()) ? context.endDate() : effectiveEnd;
        if (start.isAfter(end)) {
            return 0;
        }
        return (int) ChronoUnit.DAYS.between(start, end) + 1;
    }

    private BigDecimal resolveDailyTarget(NutritionGoal goal, BigDecimal weightKg, AnalyticsContext context) {
        BigDecimal base = goal.getTargetValue() == null ? BigDecimal.ZERO : goal.getTargetValue();
        if (goal.getTargetMode() == NutritionGoalTargetMode.PER_KG) {
            BigDecimal weight = weightKg == null ? BigDecimal.ZERO : weightKg;
            base = base.multiply(weight, MATH_CONTEXT);
        }

        NutritionGoalPeriodicity periodicity = goal.getPeriodicity() == null
                ? NutritionGoalPeriodicity.DAILY
                : goal.getPeriodicity();

        BigDecimal divisor = switch (periodicity) {
            case DAILY -> BigDecimal.ONE;
            case WEEKLY -> SEVEN;
            case MONTHLY -> THIRTY;
            case CUSTOM -> {
                int days = goal.getCustomPeriodDays() != null && goal.getCustomPeriodDays() > 0
                        ? goal.getCustomPeriodDays()
                        : context.rangeDays();
                yield BigDecimal.valueOf(days);
            }
        };

        if (divisor.compareTo(BigDecimal.ZERO) <= 0) {
            divisor = BigDecimal.ONE;
        }

        return base.divide(divisor, MATH_CONTEXT).max(BigDecimal.ZERO);
    }

    private BigDecimal resolveWeight(UUID userId, AnalyticsContext context) {
        if (userId == null) {
            return BigDecimal.ZERO;
        }
        return context.weights().computeIfAbsent(userId, id -> anamnesisRepository.findTopByUserIdOrderByIdDesc(id)
                .map(Anamnesis::getWeightKg)
                .filter(Objects::nonNull)
                .orElse(BigDecimal.ZERO));
    }

    private BigDecimal resolveWaterIntake(UUID userId, AnalyticsContext context) {
        if (userId == null) {
            return BigDecimal.ZERO;
        }
        return context.water().computeIfAbsent(userId, id -> anamnesisRepository.findTopByUserIdOrderByIdDesc(id)
                .map(Anamnesis::getWaterIntake)
                .map(this::parseWater)
                .orElse(BigDecimal.ZERO));
    }

    private boolean isUserAllowed(AnalyticsContext context, UUID userId) {
        if (context.includeAllUsers()) {
            return true;
        }
        if (CollectionUtils.isEmpty(context.userIds())) {
            return userId == null;
        }
        return userId != null && context.userIds().contains(userId);
    }

    private BigDecimal estimateWater(NutritionAnalysis analysis) {
        if (analysis == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal recorded = liquidVolumeToMilliliters(analysis);
        if (recorded.compareTo(BigDecimal.ZERO) > 0) {
            return recorded;
        }
        String source = String.join(" ",
                Optional.ofNullable(analysis.getFoodName()).orElse(""),
                Optional.ofNullable(analysis.getSummary()).orElse(""));
        boolean hinted = containsKeyword(source, "water", "água", "agua", "drink", "hydration");
        if (!hinted && analysis.getPrimaryCategory() != null) {
            hinted = containsKeyword(
                    Optional.ofNullable(analysis.getPrimaryCategory().getName()).orElse(""),
                    "water", "hydration", "drink", "beverage");
        }
        if (!hinted) {
            return BigDecimal.ZERO;
        }
        Matcher matcher = VOLUME_PATTERN.matcher(source);
        if (matcher.find()) {
            BigDecimal value = parseNumber(matcher.group(1));
            String unit = matcher.group(2).toLowerCase(Locale.ROOT);
            if (unit.startsWith("l") || unit.startsWith("lt")) {
                return value.multiply(ONE_THOUSAND, MATH_CONTEXT);
            }
            return value;
        }
        return BigDecimal.valueOf(250);
    }

    private BigDecimal liquidVolumeToMilliliters(NutritionAnalysis analysis) {
        if (analysis == null || analysis.getLiquidVolume() == null || analysis.getLiquidVolume().signum() <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal base = analysis.getLiquidVolume();
        MeasurementUnits unit = analysis.getLiquidUnit();
        if (unit == null) {
            return base;
        }
        Double factor = unit.getConversionFactor();
        if (factor != null && factor > 0) {
            return base.multiply(BigDecimal.valueOf(factor), MATH_CONTEXT);
        }
        String code = unit.getCode() != null ? unit.getCode().toLowerCase(Locale.ROOT) : "";
        return switch (code) {
            case "l", "lt", "litre", "liter" -> base.multiply(ONE_THOUSAND, MATH_CONTEXT);
            case "cup" -> base.multiply(BigDecimal.valueOf(240), MATH_CONTEXT);
            case "tbsp" -> base.multiply(BigDecimal.valueOf(15), MATH_CONTEXT);
            case "tsp" -> base.multiply(BigDecimal.valueOf(5), MATH_CONTEXT);
            default -> base;
        };
    }

    private BigDecimal estimateFiber(NutritionAnalysis analysis) {
        if (analysis == null) {
            return BigDecimal.ZERO;
        }
        String json = analysis.getCategoriesJson();
        if (StringUtils.hasText(json)) {
            try {
                JsonNode node = objectMapper.readTree(json);
                JsonNode fiber = node.has("fiber") ? node.get("fiber") : node.get("fiber_g");
                if (fiber != null && fiber.isNumber()) {
                    return BigDecimal.valueOf(fiber.asDouble());
                }
            } catch (JsonProcessingException ignored) {
            }
        }
        String summary = analysis.getSummary();
        if (StringUtils.hasText(summary)) {
            Matcher matcher = FIBER_PATTERN.matcher(summary);
            if (matcher.find()) {
                return parseNumber(matcher.group(1));
            }
        }
        return BigDecimal.ZERO;
    }

    private FoodAggregate estimateFood(NutritionAnalysis analysis) {
        if (analysis == null || !StringUtils.hasText(analysis.getFoodName())) {
            return null;
        }
        String source = String.join(" ",
                Optional.ofNullable(analysis.getFoodName()).orElse(""),
                Optional.ofNullable(analysis.getSummary()).orElse(""));

        Matcher massMatcher = MASS_PATTERN.matcher(source);
        if (massMatcher.find()) {
            BigDecimal value = parseNumber(massMatcher.group(1));
            String unit = massMatcher.group(2).toLowerCase(Locale.ROOT);
            if (unit.startsWith("kg")) {
                value = value.multiply(BigDecimal.valueOf(1000), MATH_CONTEXT);
                unit = "g";
            } else if (unit.startsWith("g")) {
                unit = "g";
            }
            return new FoodAggregate(analysis.getFoodName(), unit, value);
        }

        Matcher volumeMatcher = VOLUME_PATTERN.matcher(source);
        if (volumeMatcher.find()) {
            BigDecimal value = parseNumber(volumeMatcher.group(1));
            String unit = volumeMatcher.group(2).toLowerCase(Locale.ROOT);
            if (unit.startsWith("l") || unit.startsWith("lt")) {
                value = value.multiply(ONE_THOUSAND, MATH_CONTEXT);
            }
            return new FoodAggregate(analysis.getFoodName(), "ml", value);
        }

        BigDecimal macroSum = sumSafe(analysis.getProtein(), analysis.getCarbs(), analysis.getFat());
        if (macroSum.compareTo(BigDecimal.ZERO) > 0) {
            return new FoodAggregate(analysis.getFoodName(), "g", macroSum);
        }
        if (analysis.getCalories() != null) {
            return new FoodAggregate(analysis.getFoodName(), "kcal", analysis.getCalories());
        }
        return null;
    }

    private List<Bucket> buildBuckets(AnalyticsContext context) {
        List<Bucket> buckets = new ArrayList<>();
        LocalDate cursor;
        switch (context.groupBy()) {
            case DAY -> {
                cursor = context.startDate();
                while (!cursor.isAfter(context.endDate())) {
                    buckets.add(new Bucket(cursor, cursor, cursor));
                    cursor = cursor.plusDays(1);
                }
            }
            case WEEK -> {
                cursor = context.startDate().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                while (!cursor.isAfter(context.endDate())) {
                    LocalDate anchor = cursor;
                    LocalDate start = anchor.isBefore(context.startDate()) ? context.startDate() : anchor;
                    LocalDate end = anchor.plusDays(6);
                    if (end.isAfter(context.endDate())) {
                        end = context.endDate();
                    }
                    buckets.add(new Bucket(anchor, start, end));
                    cursor = cursor.plusWeeks(1);
                }
            }
            case MONTH -> {
                cursor = context.startDate().withDayOfMonth(1);
                while (!cursor.isAfter(context.endDate())) {
                    LocalDate anchor = cursor;
                    LocalDate monthEnd = anchor.with(TemporalAdjusters.lastDayOfMonth());
                    LocalDate start = anchor.isBefore(context.startDate()) ? context.startDate() : anchor;
                    LocalDate end = monthEnd.isAfter(context.endDate()) ? context.endDate() : monthEnd;
                    buckets.add(new Bucket(anchor, start, end));
                    cursor = cursor.plusMonths(1).withDayOfMonth(1);
                }
            }
        }
        return buckets;
    }

    private Bucket findBucket(List<Bucket> buckets, LocalDate date) {
        for (Bucket bucket : buckets) {
            if (!date.isBefore(bucket.start()) && !date.isAfter(bucket.end())) {
                return bucket;
            }
        }
        return null;
    }

    private BigDecimal parseWater(String text) {
        if (!StringUtils.hasText(text)) {
            return BigDecimal.ZERO;
        }
        Matcher matcher = VOLUME_PATTERN.matcher(text);
        if (matcher.find()) {
            BigDecimal value = parseNumber(matcher.group(1));
            String unit = matcher.group(2).toLowerCase(Locale.ROOT);
            if (unit.startsWith("l") || unit.startsWith("lt")) {
                return value.multiply(ONE_THOUSAND, MATH_CONTEXT);
            }
            return value;
        }
        Matcher fallback = NUMBER_PATTERN.matcher(text);
        if (fallback.find()) {
            return parseNumber(fallback.group(1));
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal parseNumber(String value) {
        if (!StringUtils.hasText(value)) {
            return BigDecimal.ZERO;
        }
        String normalized = value.replace(',', '.');
        try {
            return new BigDecimal(normalized, MATH_CONTEXT);
        } catch (NumberFormatException ex) {
            return BigDecimal.ZERO;
        }
    }

    private boolean containsKeyword(String source, String... keywords) {
        if (!StringUtils.hasText(source) || keywords == null) {
            return false;
        }
        String normalized = source.toLowerCase(Locale.ROOT);
        for (String keyword : keywords) {
            if (keyword != null && normalized.contains(keyword.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    private BigDecimal sumSafe(BigDecimal... values) {
        BigDecimal total = BigDecimal.ZERO;
        if (values == null) {
            return total;
        }
        for (BigDecimal value : values) {
            if (value != null) {
                total = total.add(value, MATH_CONTEXT);
            }
        }
        return total;
    }

    private BigDecimal scale(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        return value.setScale(2, ROUNDING_MODE);
    }

    private String normalizePhone(String phone) {
        if (!StringUtils.hasText(phone)) {
            return "";
        }
        return phone.replaceAll("\\D", "");
    }

    private JMException forbidden() {
        return exception(ProblemType.ANALYTICS_FORBIDDEN, HttpStatus.FORBIDDEN);
    }

    private JMException userNotFound() {
        return exception(ProblemType.ANALYTICS_USER_NOT_FOUND, HttpStatus.BAD_REQUEST);
    }

    private JMException exception(ProblemType type, HttpStatus status) {
        Locale locale = LocaleContextHolder.getLocale();
        String message = messageSource.getMessage(type.getMessageSource(), new Object[] { "" }, locale);
        return new JMException(status.value(), type.getTitle(), type.getUri(), message);
    }

    private record AnalyticsContext(
            LocalDate startDate,
            LocalDate endDate,
            int rangeDays,
            AnalyticsGroupBy groupBy,
            OffsetDateTime startDateTime,
            OffsetDateTime endDateTime,
            ZoneId zoneId,
            boolean includeAllUsers,
            Set<UUID> userIds,
            Set<String> phones,
            Map<UUID, BigDecimal> weights,
            Map<UUID, BigDecimal> water) {
    }

    private record Bucket(LocalDate anchor, LocalDate start, LocalDate end) {
        String label(AnalyticsGroupBy groupBy) {
            return switch (groupBy) {
                case DAY -> start.toString();
                case WEEK -> anchor.toString();
                case MONTH -> anchor.withDayOfMonth(1).toString();
            };
        }

        int activeDays() {
            return (int) ChronoUnit.DAYS.between(start, end) + 1;
        }
    }

    private static final class MacroTotals {
        private BigDecimal protein = BigDecimal.ZERO;
        private BigDecimal carbs = BigDecimal.ZERO;
        private BigDecimal fat = BigDecimal.ZERO;
        private BigDecimal calories = BigDecimal.ZERO;

        void add(NutritionAnalysis analysis) {
            if (analysis == null) {
                return;
            }
            protein = protein.add(defaultValue(analysis.getProtein()), MATH_CONTEXT);
            carbs = carbs.add(defaultValue(analysis.getCarbs()), MATH_CONTEXT);
            fat = fat.add(defaultValue(analysis.getFat()), MATH_CONTEXT);
            calories = calories.add(defaultValue(analysis.getCalories()), MATH_CONTEXT);
        }

        private BigDecimal defaultValue(BigDecimal value) {
            return value != null ? value : BigDecimal.ZERO;
        }

        BigDecimal protein() {
            return protein;
        }

        BigDecimal carbs() {
            return carbs;
        }

        BigDecimal fat() {
            return fat;
        }

        BigDecimal calories() {
            return calories;
        }
    }

    private static final class FoodAggregate {
        private final String name;
        private final String unit;
        private BigDecimal quantity;

        FoodAggregate(String name, String unit, BigDecimal quantity) {
            this.name = name;
            this.unit = unit;
            this.quantity = quantity == null ? BigDecimal.ZERO : quantity;
        }

        String name() {
            return name;
        }

        String unit() {
            return unit;
        }

        BigDecimal quantity() {
            return quantity;
        }

        void add(BigDecimal amount) {
            if (amount != null) {
                quantity = quantity.add(amount, MATH_CONTEXT);
            }
        }
    }

    private record AnalysisSnapshot(
            List<Bucket> buckets,
            Map<Bucket, MacroTotals> macroByBucket,
            MacroTotals totalMacros,
            Map<Bucket, BigDecimal> hydrationByBucket,
            BigDecimal totalWater,
            Map<Bucket, BigDecimal> fiberByBucket,
            BigDecimal totalFiber,
            Map<String, FoodAggregate> foods) {
    }

    private static final class BodyAggregate {
        private BigDecimal weight = BigDecimal.ZERO;
        private BigDecimal bmi = BigDecimal.ZERO;
        private BigDecimal fat = BigDecimal.ZERO;
        private BigDecimal muscle = BigDecimal.ZERO;
        private int count;

        void add(BigDecimal weightKg, BigDecimal bmiValue, BigDecimal fatPct, BigDecimal musclePct) {
            if (weightKg != null) {
                weight = weight.add(weightKg, MATH_CONTEXT);
            }
            if (bmiValue != null) {
                bmi = bmi.add(bmiValue, MATH_CONTEXT);
            }
            if (fatPct != null) {
                fat = fat.add(fatPct, MATH_CONTEXT);
            }
            if (musclePct != null) {
                muscle = muscle.add(musclePct, MATH_CONTEXT);
            }
            count++;
        }

        BigDecimal weightAvg() {
            return count == 0 ? BigDecimal.ZERO : weight.divide(BigDecimal.valueOf(count), MATH_CONTEXT);
        }

        BigDecimal bmiAvg() {
            return count == 0 ? BigDecimal.ZERO : bmi.divide(BigDecimal.valueOf(count), MATH_CONTEXT);
        }

        BigDecimal fatAvg() {
            return count == 0 ? BigDecimal.ZERO : fat.divide(BigDecimal.valueOf(count), MATH_CONTEXT);
        }

        BigDecimal muscleAvg() {
            return count == 0 ? BigDecimal.ZERO : muscle.divide(BigDecimal.valueOf(count), MATH_CONTEXT);
        }
    }
}
