package com.jm.services;

import com.jm.dto.NutritionGoalCalculationResponseDTO;
import com.jm.dto.NutritionGoalCreateRequestDTO;
import com.jm.dto.NutritionGoalDTO;
import com.jm.dto.NutritionGoalOwnerDTO;
import com.jm.entity.MeasurementUnits;
import com.jm.entity.NutritionGoal;
import com.jm.entity.NutritionGoalTemplate;
import com.jm.entity.Users;
import com.jm.enums.BiologicalSex;
import com.jm.enums.NutritionGoalPeriodicity;
import com.jm.enums.NutritionGoalTargetMode;
import com.jm.enums.NutritionGoalType;
import com.jm.enums.NutritionGoalObjective;
import com.jm.execption.JMException;
import com.jm.execption.ProblemType;
import com.jm.mappers.NutritionGoalMapper;
import com.jm.repository.MeasurementUnitRepository;
import com.jm.repository.NutritionGoalRepository;
import com.jm.repository.NutritionGoalTemplateRepository;
import com.jm.repository.UserRepository;
import com.jm.speciation.NutritionGoalSpecification;
import com.jm.utils.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class NutritionGoalService {

    private static final Logger logger = LoggerFactory.getLogger(NutritionGoalService.class);
    private static final double DEFAULT_ACTIVITY_FACTOR = 1.55;
    private static final double FAT_PERCENTAGE = 0.275;
    private static final int CALORIE_ROUNDING_STEP = 10;
    private static final int MACRO_ROUNDING_STEP = 5;

    private final NutritionGoalRepository repository;
    private final NutritionGoalMapper mapper;
    private final NutritionGoalTemplateRepository templateRepository;
    private final MeasurementUnitRepository measurementUnitRepository;
    private final UserRepository userRepository;
    private final MessageSource messageSource;

    public NutritionGoalService(NutritionGoalRepository repository, NutritionGoalMapper mapper,
            NutritionGoalTemplateRepository templateRepository, MeasurementUnitRepository measurementUnitRepository,
            UserRepository userRepository, MessageSource messageSource) {
        this.repository = repository;
        this.mapper = mapper;
        this.templateRepository = templateRepository;
        this.measurementUnitRepository = measurementUnitRepository;
        this.userRepository = userRepository;
        this.messageSource = messageSource;
    }

    @Transactional(readOnly = true)
    public Page<NutritionGoalDTO> findAll(Pageable pageable, NutritionGoalDTO filter) {
        NutritionGoalDTO criteria = filter != null ? filter : new NutritionGoalDTO();
        if (isClient()) {
            SecurityUtils.getCurrentUserId().ifPresent(criteria::setCreatedByUserId);
        }
        Page<NutritionGoal> page = repository.findAll(NutritionGoalSpecification.search(criteria), pageable);
        return page.map(mapper::toDTO);
    }

    @Transactional(readOnly = true)
    public NutritionGoalDTO findById(UUID id) {
        NutritionGoal entity = repository.findById(id).orElseThrow(this::goalNotFound);
        enforceOwnership(entity);
        return mapper.toDTO(entity);
    }

    @Transactional
    public NutritionGoalDTO create(NutritionGoalDTO dto) {
        return save(dto, false);
    }

    @Transactional
    public NutritionGoalDTO update(NutritionGoalDTO dto) {
        if (dto == null || dto.getId() == null) {
            throw invalidBodyException();
        }
        return save(dto, true);
    }

    @Transactional
    public void delete(UUID id) {
        NutritionGoal entity = repository.findById(id).orElseThrow(this::goalNotFound);
        enforceOwnership(entity);
        repository.delete(entity);
        logger.debug("{} - {}", getMessage("goal.deleted"), id);
    }

    @Transactional
    public NutritionGoalCalculationResponseDTO calculateAndSaveUserGoals(NutritionGoalCreateRequestDTO request) {
        if (request == null || request.getUserId() == null) {
            throw invalidBodyException();
        }

        Users user = userRepository.findById(request.getUserId()).orElseThrow(this::userNotFound);

        BiologicalSex sex = request.getSex();
        if (sex == null) {
            throw invalidDataException("goal.ai.invalid.sex");
        }

        Integer age = request.getAge();
        if (age == null || age <= 0) {
            throw invalidDataException("goal.ai.invalid.age");
        }

        if (!isPositive(request.getWeight())) {
            throw invalidDataException("goal.ai.invalid.weight");
        }
        if (!isPositive(request.getHeight())) {
            throw invalidDataException("goal.ai.invalid.height");
        }

        double weightKg = convertWeightToKg(request.getWeight(), request.getWeightUnit());
        double heightCm = convertHeightToCm(request.getHeight(), request.getHeightUnit(), request.getHeightInches());

        double bmr = calculateBmr(sex, weightKg, heightCm, age);
        double activityFactor = request.getActivityFactor() != null && request.getActivityFactor() > 0
                ? request.getActivityFactor()
                : DEFAULT_ACTIVITY_FACTOR;

        NutritionGoalObjective objective = request.getObjective() != null ? request.getObjective()
                : NutritionGoalObjective.MAINTENANCE;

        double tdee = bmr * activityFactor;
        double recommendedCalories = tdee * objective.getCalorieMultiplier();

        double bmrRounded = roundToStep(bmr, CALORIE_ROUNDING_STEP);
        double tdeeRounded = roundToStep(tdee, CALORIE_ROUNDING_STEP);
        double recommendedRounded = roundToStep(recommendedCalories, CALORIE_ROUNDING_STEP);

        double proteinGrams = roundToStep(weightKg * objective.getProteinPerKg(), MACRO_ROUNDING_STEP);
        double fatGrams = roundToStep((recommendedRounded * FAT_PERCENTAGE) / 9.0, MACRO_ROUNDING_STEP);
        double carbsBase = (recommendedRounded - (proteinGrams * 4 + fatGrams * 9)) / 4.0;
        double carbsGrams = roundToStep(Math.max(carbsBase, 0), MACRO_ROUNDING_STEP);

        MeasurementUnits calorieUnit = resolveMeasurementUnitByCodes("KCAL");
        MeasurementUnits gramUnit = resolveMeasurementUnitByCodes("GRAM", "G");

        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusYears(1);

        Locale locale = LocaleContextHolder.getLocale();
        String objectiveLabel = resolveObjectiveLabel(objective, locale);
        String summary = buildSummaryMessage(locale, bmrRounded, tdeeRounded, recommendedRounded, objectiveLabel,
                proteinGrams, carbsGrams, fatGrams);

        NutritionGoal calorieGoal = NutritionGoal.builder()
                .type(NutritionGoalType.ENERGY)
                .targetValue(toBigDecimal(recommendedRounded))
                .unit(calorieUnit)
                .periodicity(NutritionGoalPeriodicity.DAILY)
                .targetMode(NutritionGoalTargetMode.ABSOLUTE)
                .startDate(startDate)
                .endDate(endDate)
                .createdBy(user)
                .active(Boolean.TRUE)
                .notes(summary)
                .build();

        NutritionGoal proteinGoal = buildMacroGoal(user, gramUnit, startDate, endDate, NutritionGoalType.PROTEIN,
                proteinGrams, macroNote(locale, "goal.ai.macro.label.protein", proteinGrams));
        NutritionGoal carbsGoal = buildMacroGoal(user, gramUnit, startDate, endDate, NutritionGoalType.CARBOHYDRATE,
                carbsGrams, macroNote(locale, "goal.ai.macro.label.carbs", carbsGrams));
        NutritionGoal fatGoal = buildMacroGoal(user, gramUnit, startDate, endDate, NutritionGoalType.FAT,
                fatGrams, macroNote(locale, "goal.ai.macro.label.fat", fatGrams));

        List<NutritionGoal> goals = new ArrayList<>();
        goals.add(calorieGoal);
        goals.add(proteinGoal);
        goals.add(carbsGoal);
        goals.add(fatGoal);

        List<NutritionGoal> saved = repository.saveAll(goals);
        logger.debug("Calculated AI nutrition goals for user {}", user.getId());

        List<NutritionGoalDTO> result = saved.stream().map(mapper::toDTO).collect(Collectors.toList());

        return NutritionGoalCalculationResponseDTO.builder()
                .bmr(bmrRounded)
                .tdee(tdeeRounded)
                .recommendedCalories(recommendedRounded)
                .protein(proteinGrams)
                .carbs(carbsGrams)
                .fat(fatGrams)
                .summary(summary)
                .goals(result)
                .build();
    }

    @Transactional(readOnly = true)
    public List<NutritionGoalOwnerDTO> listOwners(String query) {
        if (isClient()) {
            UUID currentUserId = SecurityUtils.getCurrentUserId().orElseThrow(this::invalidBodyException);
            Users currentUser = userRepository.findById(currentUserId).orElseThrow(this::goalForbidden);
            return List.of(toOwnerDto(currentUser));
        }

        List<Users> users;
        if (StringUtils.hasText(query)) {
            Map<UUID, Users> accumulator = new LinkedHashMap<>();
            userRepository.findTop20ByTypeAndNameContainingIgnoreCaseOrderByNameAsc(Users.Type.CLIENT, query)
                    .forEach(user -> accumulator.put(user.getId(), user));
            userRepository.findTop20ByTypeAndEmailContainingIgnoreCaseOrderByEmailAsc(Users.Type.CLIENT, query)
                    .forEach(user -> accumulator.putIfAbsent(user.getId(), user));
            users = new ArrayList<>(accumulator.values());
        } else {
            users = userRepository.findTop50ByTypeOrderByNameAsc(Users.Type.CLIENT);
        }

        return users.stream().map(this::toOwnerDto).collect(Collectors.toList());
    }

    private NutritionGoalDTO save(NutritionGoalDTO dto, boolean updating) {
        if (dto == null) {
            throw invalidBodyException();
        }

        NutritionGoal entity;
        if (updating) {
            entity = repository.findById(dto.getId()).orElseThrow(this::goalNotFound);
            enforceOwnership(entity);
            mapper.updateEntityFromDto(dto, entity);
        } else {
            entity = mapper.toEntity(dto);
            entity.setActive(Boolean.TRUE);
        }

        applyOwner(dto, entity);
        applyUnit(dto, entity);
        applyTemplate(dto, entity);

        if (dto.getTargetMode() != null) {
            entity.setTargetMode(dto.getTargetMode());
        } else if (entity.getTargetMode() == null) {
            entity.setTargetMode(NutritionGoalTargetMode.ABSOLUTE);
        }

        if (dto.getPeriodicity() != null) {
            entity.setPeriodicity(dto.getPeriodicity());
        }

        if (entity.getPeriodicity() == null) {
            entity.setPeriodicity(NutritionGoalPeriodicity.DAILY);
        }

        if (entity.getPeriodicity() != null && entity.getPeriodicity() != NutritionGoalPeriodicity.CUSTOM) {
            entity.setCustomPeriodDays(null);
        }

        if (dto.getCustomPeriodDays() != null && entity.getPeriodicity() == NutritionGoalPeriodicity.CUSTOM) {
            entity.setCustomPeriodDays(dto.getCustomPeriodDays());
        }

        if (entity.getActive() == null) {
            entity.setActive(Boolean.TRUE);
        }
        if (dto.getActive() != null) {
            entity.setActive(dto.getActive());
        }

        NutritionGoal saved = repository.save(entity);
        logger.debug("{} - {}", getMessage("goal.saved"), saved.getId());
        return mapper.toDTO(saved);
    }

    private NutritionGoal buildMacroGoal(Users user, MeasurementUnits unit, LocalDate startDate, LocalDate endDate,
            NutritionGoalType type, double grams, String notes) {
        return NutritionGoal.builder()
                .type(type)
                .targetValue(toBigDecimal(grams))
                .unit(unit)
                .periodicity(NutritionGoalPeriodicity.DAILY)
                .targetMode(NutritionGoalTargetMode.ABSOLUTE)
                .startDate(startDate)
                .endDate(endDate)
                .createdBy(user)
                .active(Boolean.TRUE)
                .notes(notes)
                .build();
    }

    private JMException invalidDataException(String messageKey) {
        Locale locale = LocaleContextHolder.getLocale();
        String detail = messageSource.getMessage(messageKey, null, locale);
        return new JMException(HttpStatus.BAD_REQUEST.value(), ProblemType.INVALID_DATA.getTitle(),
                ProblemType.INVALID_DATA.getUri(), detail);
    }

    private boolean isPositive(Double value) {
        return value != null && Double.isFinite(value) && value > 0;
    }

    private double convertWeightToKg(Double weight, String unit) {
        double value = weight != null ? weight : 0.0;
        if (!StringUtils.hasText(unit)) {
            return value;
        }
        String normalized = unit.trim().toLowerCase(Locale.ROOT);
        if (normalized.matches("kg|kgs|kilogram|kilograms|kilo|kilos")) {
            return value;
        }
        if (normalized.matches("lb|lbs|pound|pounds")) {
            return value / 2.20462;
        }
        throw invalidDataException("goal.ai.invalid.weight-unit");
    }

    private double convertHeightToCm(Double height, String unit, Double inches) {
        double value = height != null ? height : 0.0;
        if (!StringUtils.hasText(unit)) {
            return value;
        }
        String normalized = unit.trim().toLowerCase(Locale.ROOT);
        switch (normalized) {
            case "cm", "centimeter", "centimeters" -> {
                return value;
            }
            case "m", "meter", "meters" -> {
                return value * 100;
            }
            case "ft", "feet", "ft_in" -> {
                double inchesValue = inches != null ? inches : 0.0;
                return (value * 30.48) + (inchesValue * 2.54);
            }
            case "in", "inch", "inches" -> {
                return value * 2.54;
            }
            default -> throw invalidDataException("goal.ai.invalid.height-unit");
        }
    }

    private double calculateBmr(BiologicalSex sex, double weightKg, double heightCm, int age) {
        if (!Double.isFinite(weightKg) || weightKg <= 0) {
            throw invalidDataException("goal.ai.invalid.weight");
        }
        if (!Double.isFinite(heightCm) || heightCm <= 0) {
            throw invalidDataException("goal.ai.invalid.height");
        }
        double base = (10 * weightKg) + (6.25 * heightCm) - (5 * age);
        return sex == BiologicalSex.MALE ? base + 5 : base - 161;
    }

    private double roundToStep(double value, int step) {
        if (!Double.isFinite(value) || step <= 0) {
            return 0.0;
        }
        return Math.round(value / step) * step;
    }

    private BigDecimal toBigDecimal(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP);
    }

    private MeasurementUnits resolveMeasurementUnitByCodes(String... codes) {
        if (codes == null || codes.length == 0) {
            throw unitNotFound();
        }
        for (String code : codes) {
            if (!StringUtils.hasText(code)) {
                continue;
            }
            Optional<MeasurementUnits> unit = measurementUnitRepository.findByCodeIgnoreCase(code);
            if (unit.isPresent()) {
                return unit.get();
            }
        }
        throw unitNotFound();
    }

    private String resolveObjectiveLabel(NutritionGoalObjective objective, Locale locale) {
        NutritionGoalObjective safeObjective = objective != null ? objective : NutritionGoalObjective.MAINTENANCE;
        try {
            return messageSource.getMessage(safeObjective.getMessageKey(), null, locale);
        } catch (Exception ex) {
            String normalized = safeObjective.name().toLowerCase(Locale.ROOT).replace('_', ' ');
            return StringUtils.capitalize(normalized);
        }
    }

    private String buildSummaryMessage(Locale locale, double bmr, double tdee, double calories, String objectiveLabel,
            double protein, double carbs, double fat) {
        Object[] args = new Object[] {
                formatNumber(bmr, locale),
                formatNumber(tdee, locale),
                formatNumber(calories, locale),
                objectiveLabel,
                formatNumber(protein, locale),
                formatNumber(carbs, locale),
                formatNumber(fat, locale)
        };
        try {
            return messageSource.getMessage("goal.ai.summary", args, locale);
        } catch (Exception ex) {
            return String.format(locale,
                    "BMR: %s kcal/dia | TDEE: %s kcal/dia | Calorias: %s kcal (%s) | Proteínas: %s g | Carboidratos: %s g | Gorduras: %s g",
                    args);
        }
    }

    private String macroNote(Locale locale, String labelKey, double grams) {
        String label;
        try {
            label = messageSource.getMessage(labelKey, null, locale);
        } catch (Exception ex) {
            String normalized = labelKey.replace("goal.ai.macro.label.", "");
            label = StringUtils.capitalize(normalized);
        }
        String amount = formatNumber(grams, locale);
        try {
            return messageSource.getMessage("goal.ai.macro.note", new Object[] { label, amount }, locale);
        } catch (Exception ex) {
            return label + ": " + amount + " g/dia";
        }
    }

    private String formatNumber(double value, Locale locale) {
        NumberFormat format = NumberFormat.getNumberInstance(locale);
        format.setMaximumFractionDigits(0);
        format.setMinimumFractionDigits(0);
        return format.format(Math.round(value));
    }

    private void applyOwner(NutritionGoalDTO dto, NutritionGoal entity) {
        UUID ownerId = dto.getCreatedByUserId();
        if (isClient()) {
            UUID currentUserId = SecurityUtils.getCurrentUserId().orElseThrow(this::goalForbidden);
            if (ownerId != null && !ownerId.equals(currentUserId)) {
                throw goalForbidden();
            }
            Users currentUser = userRepository.findById(currentUserId).orElseThrow(this::goalForbidden);
            entity.setCreatedBy(currentUser);
            return;
        }

        UUID resolvedOwnerId = Optional.ofNullable(ownerId)
                .or(() -> SecurityUtils.getCurrentUserId())
                .orElse(null);
        if (resolvedOwnerId == null) {
            throw invalidBodyException();
        }
        Users owner = userRepository.findById(resolvedOwnerId).orElseThrow(this::userNotFound);
        entity.setCreatedBy(owner);
    }

    private void applyUnit(NutritionGoalDTO dto, NutritionGoal entity) {
        if (dto.getUnitId() == null) {
            throw invalidBodyException();
        }
        MeasurementUnits unit = measurementUnitRepository.findById(dto.getUnitId())
                .orElseThrow(this::unitNotFound);
        entity.setUnit(unit);
    }

    private void applyTemplate(NutritionGoalDTO dto, NutritionGoal entity) {
        if (dto.getTemplateId() == null) {
            entity.setTemplate(null);
            return;
        }
        NutritionGoalTemplate template = templateRepository.findById(dto.getTemplateId())
                .orElseThrow(this::templateNotFound);
        entity.setTemplate(template);
    }

    private void enforceOwnership(NutritionGoal entity) {
        if (!isClient()) {
            return;
        }
        UUID currentUserId = SecurityUtils.getCurrentUserId().orElse(null);
        if (currentUserId == null) {
            throw goalForbidden();
        }
        UUID ownerId = entity.getCreatedBy() != null ? entity.getCreatedBy().getId() : null;
        if (!Objects.equals(currentUserId, ownerId)) {
            throw goalForbidden();
        }
    }

    private NutritionGoalOwnerDTO toOwnerDto(Users user) {
        if (user == null) {
            return null;
        }
        String displayName = user.getName();
        if (StringUtils.hasText(user.getLastName())) {
            displayName = (displayName != null ? displayName + " " : "") + user.getLastName();
        }
        if (!StringUtils.hasText(displayName)) {
            displayName = user.getEmail();
        }
        return NutritionGoalOwnerDTO.builder()
                .id(user.getId())
                .displayName(displayName)
                .email(user.getEmail())
                .build();
    }

    private JMException goalNotFound() {
        return buildException(ProblemType.NUTRITION_GOAL_NOT_FOUND, HttpStatus.BAD_REQUEST);
    }

    private JMException goalForbidden() {
        return buildException(ProblemType.NUTRITION_GOAL_FORBIDDEN, HttpStatus.FORBIDDEN);
    }

    private JMException templateNotFound() {
        return buildException(ProblemType.NUTRITION_GOAL_TEMPLATE_NOT_FOUND, HttpStatus.BAD_REQUEST);
    }

    private JMException unitNotFound() {
        return buildException(ProblemType.NUTRITION_GOAL_UNIT_NOT_FOUND, HttpStatus.BAD_REQUEST);
    }

    private JMException userNotFound() {
        return buildException(ProblemType.USER_NOT_FOUND, HttpStatus.BAD_REQUEST);
    }

    private JMException invalidBodyException() {
        return buildException(ProblemType.INVALID_BODY, HttpStatus.BAD_REQUEST);
    }

    private JMException buildException(ProblemType type, HttpStatus status) {
        Locale locale = LocaleContextHolder.getLocale();
        String messageDetails = messageSource.getMessage(type.getMessageSource(), new Object[] { "" }, locale);
        return new JMException(status.value(), type.getTitle(), type.getUri(), messageDetails);
    }

    private String getMessage(String key) {
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(key, new Object[] { "" }, locale);
    }

    private boolean isClient() {
        return SecurityUtils.hasRole("CLIENT");
    }
}
