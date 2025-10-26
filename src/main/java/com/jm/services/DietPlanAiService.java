package com.jm.services;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jm.dto.AiPromptReferenceDTO;
import com.jm.dto.DietMealDTO;
import com.jm.dto.DietMealItemDTO;
import com.jm.dto.DietPlanAiJobDTO;
import com.jm.dto.DietPlanAiSuggestionRequest;
import com.jm.dto.DietPlanDTO;
import com.jm.entity.Food;
import com.jm.entity.MeasurementUnits;
import com.jm.enums.AiProvider;
import com.jm.enums.DietPlanAiJobStatus;
import com.jm.enums.DietMealType;
import com.jm.execption.JMException;
import com.jm.execption.ProblemType;
import com.jm.entity.DietPlanAiJob;
import com.jm.repository.FoodRepository;
import com.jm.repository.MeasurementUnitRepository;
import com.jm.repository.DietPlanAiJobRepository;
import com.jm.services.ai.AiClient;
import com.jm.services.ai.AiClientFactory;
import com.jm.services.ai.AiRequest;
import com.jm.services.ai.AiRequestType;
import com.jm.services.ai.AiResponse;
import com.jm.services.AiPromptReferenceService;
import com.jm.utils.SecurityUtils;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Service
public class DietPlanAiService {

    private static final Logger logger = LoggerFactory.getLogger(DietPlanAiService.class);
    private static final DateTimeFormatter STRICT_TIME = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter LENIENT_TIME = DateTimeFormatter.ofPattern("H:mm");

    private final AiClientFactory aiClientFactory;
    private final AiPromptReferenceService aiPromptReferenceService;
    private final ObjectMapper objectMapper;
    private final DietPlanService dietPlanService;
    private final FoodRepository foodRepository;
    private final MeasurementUnitRepository measurementUnitRepository;
    private final DietPlanAiJobRepository jobRepository;
    private final MessageSource messageSource;

    @Value("${diet.ai.prompt-code:PLAN_DIET}")
    private String promptCode;

    @Value("${diet.ai.timeout-seconds:45}")
    private long timeoutSeconds;

    public DietPlanAiService(AiClientFactory aiClientFactory, AiPromptReferenceService aiPromptReferenceService,
            ObjectMapper objectMapper, DietPlanService dietPlanService, FoodRepository foodRepository,
            MeasurementUnitRepository measurementUnitRepository, DietPlanAiJobRepository jobRepository,
            MessageSource messageSource) {
        this.aiClientFactory = aiClientFactory;
        this.aiPromptReferenceService = aiPromptReferenceService;
        this.objectMapper = objectMapper;
        this.dietPlanService = dietPlanService;
        this.foodRepository = foodRepository;
        this.measurementUnitRepository = measurementUnitRepository;
        this.jobRepository = jobRepository;
        this.messageSource = messageSource;
    }

    public DietPlanAiJobDTO requestSuggestion(DietPlanAiSuggestionRequest request) {
        if (request == null) {
            throw buildException(ProblemType.INVALID_BODY, HttpStatus.BAD_REQUEST, "invalid_message_body");
        }

        DietPlanAiSuggestionRequest payload = cloneRequest(request);

        DietPlanAiJob job = DietPlanAiJob.builder()
                .status(DietPlanAiJobStatus.PROCESSING)
                .requestedByUserId(SecurityUtils.getCurrentUserId().orElse(null))
                .build();

        job = jobRepository.save(job);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Locale locale = LocaleContextHolder.getLocale();

        DietPlanAiJob persistedJob = job;
        CompletableFuture.runAsync(() -> {
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            LocaleContextHolder.setLocale(locale);
            try {
                processJob(persistedJob.getId(), payload);
            } finally {
                SecurityContextHolder.clearContext();
                LocaleContextHolder.resetLocaleContext();
            }
        });

        return mapToDto(job, null);
    }

    public DietPlanAiJobDTO findJob(UUID jobId) {
        DietPlanAiJob job = jobRepository.findById(jobId)
                .orElseThrow(() -> buildException(ProblemType.INVALID_DATA, HttpStatus.NOT_FOUND, "diet.ai.job-not-found"));

        DietPlanDTO diet = null;
        if (job.getStatus() == DietPlanAiJobStatus.COMPLETED && job.getDietPlanId() != null) {
            diet = dietPlanService.findById(job.getDietPlanId());
        }

        return mapToDto(job, diet);
    }

    private void processJob(UUID jobId, DietPlanAiSuggestionRequest request) {
        try {
            DietPlanDTO result = generateSuggestion(request);
            jobRepository.findById(jobId).ifPresent(job -> {
                job.setStatus(DietPlanAiJobStatus.COMPLETED);
                job.setDietPlanId(result != null ? result.getId() : null);
                job.setErrorMessage(null);
                jobRepository.save(job);
            });
        } catch (JMException ex) {
            logger.warn("AI diet suggestion failed: {}", ex.getDetails(), ex);
            updateJobFailure(jobId, ex.getDetails());
        } catch (Exception ex) {
            logger.error("Unexpected error generating AI diet suggestion", ex);
            String message = messageSource.getMessage("diet.ai.unexpected-error", null, "diet.ai.unexpected-error",
                    LocaleContextHolder.getLocale());
            updateJobFailure(jobId, message);
        }
    }

    private void updateJobFailure(UUID jobId, String message) {
        jobRepository.findById(jobId).ifPresent(job -> {
            job.setStatus(DietPlanAiJobStatus.FAILED);
            job.setErrorMessage(message);
            jobRepository.save(job);
        });
    }

    private DietPlanAiSuggestionRequest cloneRequest(DietPlanAiSuggestionRequest source) {
        DietPlanAiSuggestionRequest copy = new DietPlanAiSuggestionRequest();
        copy.setDietId(source.getDietId());
        copy.setOwnerId(source.getOwnerId());
        copy.setPatientName(source.getPatientName());
        copy.setGoal(source.getGoal());
        copy.setNotes(source.getNotes());
        copy.setDayOfWeek(source.getDayOfWeek());
        copy.setActive(source.getActive());
        return copy;
    }

    private DietPlanDTO generateSuggestion(DietPlanAiSuggestionRequest request) {
        AiPromptContext context = resolvePromptContext();
        String prompt = populatePrompt(context.promptTemplate(), request);

        AiClient client;
        try {
            client = aiClientFactory.createClient(context.provider());
        } catch (IllegalArgumentException ex) {
            logger.error("No AI client available for provider {}", context.provider(), ex);
            throw buildException(ProblemType.INVALID_DATA, HttpStatus.BAD_REQUEST, "diet.ai.unavailable");
        }

        AiRequest aiRequest = AiRequest.builder()
                .type(AiRequestType.TEXT)
                .model(context.model())
                .prompt(prompt)
                .from("IA-DietPlanService")
                .userId(SecurityUtils.getCurrentUserId().orElse(null))
                .timeout(Duration.ofSeconds(Math.max(timeoutSeconds, 1)))
                .build();

        AiResponse response = client.execute(aiRequest);
        String content = response != null ? response.content() : null;
        if (!StringUtils.hasText(content)) {
            throw buildException(ProblemType.INVALID_DATA, HttpStatus.BAD_REQUEST, "diet.ai.invalid-response");
        }

        DietPlanPayload payload = parseResponse(content);
        DietPlanDTO dto = mapToDietPlan(payload, request);
        return dietPlanService.create(dto);
    }

    private DietPlanAiJobDTO mapToDto(DietPlanAiJob job, DietPlanDTO diet) {
        if (job == null) {
            return null;
        }
        return DietPlanAiJobDTO.builder()
                .id(job.getId())
                .status(job.getStatus())
                .errorMessage(job.getErrorMessage())
                .dietPlanId(job.getDietPlanId())
                .diet(diet)
                .createdAt(job.getCreatedAt())
                .updatedAt(job.getUpdatedAt())
                .build();
    }

    private AiPromptContext resolvePromptContext() {
        Optional<AiPromptReferenceDTO> reference = aiPromptReferenceService.resolvePrompt(promptCode);
        AiPromptReferenceDTO promptReference = reference
                .orElseThrow(() -> buildException(ProblemType.INVALID_DATA, HttpStatus.BAD_REQUEST,
                        "diet.ai.prompt-not-found"));

        AiProvider provider = promptReference.getProvider();
        String model = promptReference.getModel();
        String prompt = promptReference.getPrompt();

        if (provider == null || !StringUtils.hasText(model)) {
            throw buildException(ProblemType.INVALID_DATA, HttpStatus.BAD_REQUEST, "diet.ai.unavailable");
        }

        if (!StringUtils.hasText(prompt)) {
            throw buildException(ProblemType.INVALID_DATA, HttpStatus.BAD_REQUEST, "diet.ai.prompt-not-found");
        }

        return new AiPromptContext(provider, model.trim(), prompt.trim());
    }

    private String populatePrompt(String template, DietPlanAiSuggestionRequest request) {
        Map<String, String> values = Map.of(
                "{{patient_name}}", defaultString(request.getPatientName()),
                "{{goal}}", defaultString(request.getGoal()),
                "{{notes}}", defaultString(request.getNotes()));
        String result = template;
        for (Map.Entry<String, String> entry : values.entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue());
        }
        DayOfWeek dayOfWeek = Optional.ofNullable(request.getDayOfWeek()).orElse(DayOfWeek.MONDAY);
        result = result.replace("{{day_of_week}}", dayOfWeek.name());
        return result;
    }

    private String defaultString(String value) {
        return StringUtils.hasText(value) ? value.trim() : "";
    }

    private DietPlanPayload parseResponse(String content) {
        String sanitized = sanitizeResponse(content);
        try {
            ObjectMapper mapper = objectMapper.copy()
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            return mapper.readValue(sanitized, DietPlanPayload.class);
        } catch (Exception ex) {
            logger.error("Failed to parse AI diet suggestion response: {}", sanitized, ex);
            throw buildException(ProblemType.INVALID_DATA, HttpStatus.BAD_REQUEST, "diet.ai.invalid-response");
        }
    }

    private String sanitizeResponse(String content) {
        String trimmed = content.trim();
        if (trimmed.startsWith("```") && trimmed.endsWith("```")) {
            int firstLineBreak = trimmed.indexOf('\n');
            if (firstLineBreak >= 0) {
                trimmed = trimmed.substring(firstLineBreak + 1);
            }
            int lastFence = trimmed.lastIndexOf("```");
            if (lastFence >= 0) {
                trimmed = trimmed.substring(0, lastFence);
            }
        }
        return trimmed.trim();
    }

    private DietPlanDTO mapToDietPlan(DietPlanPayload payload, DietPlanAiSuggestionRequest request) {
        if (payload == null) {
            throw buildException(ProblemType.INVALID_DATA, HttpStatus.BAD_REQUEST, "diet.ai.invalid-response");
        }
        DietPlanDTO dto = new DietPlanDTO();
        dto.setId(request.getDietId());
        dto.setCreatedByUserId(request.getOwnerId());
        dto.setPatientName(resolveValue(payload.patientName(), request.getPatientName()));
        dto.setNotes(resolveValue(payload.notes(), request.getNotes()));
        Boolean active = payload.active();
        if (active == null) {
            active = request.getActive();
        }
        dto.setActive(active != null ? active : Boolean.TRUE);
        dto.setDayOfWeek(resolveDayOfWeek(payload.dayOfWeek(), request.getDayOfWeek()));
        dto.setMeals(mapMeals(payload.meals()));
        return dto;
    }

    private String resolveValue(String primary, String fallback) {
        if (StringUtils.hasText(primary)) {
            return primary.trim();
        }
        return defaultString(fallback);
    }

    private DayOfWeek resolveDayOfWeek(String value, DayOfWeek fallback) {
        if (StringUtils.hasText(value)) {
            try {
                return DayOfWeek.valueOf(value.trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ex) {
                logger.warn("Invalid day of week from AI response: {}", value);
            }
        }
        return fallback != null ? fallback : DayOfWeek.MONDAY;
    }

    private List<DietMealDTO> mapMeals(List<MealPayload> meals) {
        if (CollectionUtils.isEmpty(meals)) {
            throw buildException(ProblemType.INVALID_DATA, HttpStatus.BAD_REQUEST, "diet.ai.missing-meals");
        }
        List<DietMealDTO> mappedMeals = new ArrayList<>();
        for (MealPayload meal : meals) {
            DietMealDTO dto = new DietMealDTO();
            dto.setMealType(resolveMealType(meal.mealType()));
            dto.setScheduledTime(resolveTime(meal.scheduledTime()));
            dto.setItems(mapItems(meal.items()));
            mappedMeals.add(dto);
        }
        return mappedMeals;
    }

    private DietMealType resolveMealType(String value) {
        if (!StringUtils.hasText(value)) {
            throw buildException(ProblemType.INVALID_DATA, HttpStatus.BAD_REQUEST, "diet.ai.invalid-response");
        }
        try {
            return DietMealType.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw buildException(ProblemType.INVALID_DATA, HttpStatus.BAD_REQUEST, "diet.ai.invalid-response");
        }
    }

    private LocalTime resolveTime(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String normalized = value.trim();
        try {
            if (normalized.length() == 5) {
                return LocalTime.parse(normalized, STRICT_TIME);
            }
            if (normalized.length() == 4) {
                return LocalTime.parse(normalized, LENIENT_TIME);
            }
            return LocalTime.parse(normalized);
        } catch (DateTimeParseException ex) {
            throw buildException(ProblemType.INVALID_DATA, HttpStatus.BAD_REQUEST, "diet.ai.invalid-time", normalized);
        }
    }

    private List<DietMealItemDTO> mapItems(List<ItemPayload> items) {
        if (CollectionUtils.isEmpty(items)) {
            throw buildException(ProblemType.INVALID_DATA, HttpStatus.BAD_REQUEST, "diet.ai.invalid-response");
        }
        List<DietMealItemDTO> mapped = new ArrayList<>();
        for (ItemPayload item : items) {
            BigDecimal quantity = item.quantity();
            if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
                throw buildException(ProblemType.INVALID_DATA, HttpStatus.BAD_REQUEST, "diet.ai.invalid-response");
            }
            Food food = resolveFood(item);
            MeasurementUnits unit = resolveUnit(item);
            DietMealItemDTO dto = new DietMealItemDTO();
            dto.setFoodItemId(food.getId());
            dto.setFoodItemName(food.getName());
            dto.setUnitId(unit.getId());
            String unitLabel = StringUtils.hasText(unit.getDescription()) ? unit.getDescription() : unit.getSymbol();
            dto.setUnitDisplayName(unitLabel);
            dto.setQuantity(quantity);
            dto.setNotes(StringUtils.hasText(item.notes()) ? item.notes().trim() : null);
            mapped.add(dto);
        }
        return mapped;
    }

    private Food resolveFood(ItemPayload item) {
        List<String> candidates = new ArrayList<>();
        UUID parsedId = parseUuid(item.foodId());
        if (parsedId != null) {
            return foodRepository.findById(parsedId).orElseThrow(() -> buildException(ProblemType.DIET_FOOD_NOT_FOUND,
                    HttpStatus.BAD_REQUEST, "diet.ai.food-unresolved", parsedId.toString()));
        }
        if (StringUtils.hasText(item.foodCode())) {
            candidates.add(item.foodCode());
            Optional<Food> byCode = foodRepository.findFirstByCodeIgnoreCase(item.foodCode().trim());
            if (byCode.isPresent()) {
                return byCode.get();
            }
        }
        if (StringUtils.hasText(item.foodName())) {
            String normalized = item.foodName().trim();
            candidates.add(normalized);
            Optional<Food> exact = foodRepository.findFirstByNameIgnoreCase(normalized);
            if (exact.isPresent()) {
                return exact.get();
            }
            Optional<Food> partial = foodRepository.findFirstByNameContainingIgnoreCase(normalized);
            if (partial.isPresent()) {
                return partial.get();
            }
        }
        String label = candidates.isEmpty() ? "unknown" : candidates.get(candidates.size() - 1);
        throw buildException(ProblemType.DIET_FOOD_NOT_FOUND, HttpStatus.BAD_REQUEST, "diet.ai.food-unresolved", label);
    }

    private MeasurementUnits resolveUnit(ItemPayload item) {
        UUID parsedId = parseUuid(item.unitId());
        if (parsedId != null) {
            return measurementUnitRepository.findById(parsedId)
                    .orElseThrow(() -> buildException(ProblemType.DIET_UNIT_NOT_FOUND, HttpStatus.BAD_REQUEST,
                            "diet.ai.unit-unresolved", parsedId.toString()));
        }
        if (StringUtils.hasText(item.unitCode())) {
            Optional<MeasurementUnits> byCode = measurementUnitRepository.findByCodeIgnoreCase(item.unitCode().trim());
            if (byCode.isPresent()) {
                return byCode.get();
            }
        }
        if (StringUtils.hasText(item.unitName())) {
            String normalized = item.unitName().trim();
            Optional<MeasurementUnits> byDescription = measurementUnitRepository
                    .findFirstByDescriptionIgnoreCase(normalized);
            if (byDescription.isPresent()) {
                return byDescription.get();
            }
            Optional<MeasurementUnits> bySymbol = measurementUnitRepository.findFirstBySymbolIgnoreCase(normalized);
            if (bySymbol.isPresent()) {
                return bySymbol.get();
            }
        }
        String label = StringUtils.hasText(item.unitCode()) ? item.unitCode() : item.unitName();
        throw buildException(ProblemType.DIET_UNIT_NOT_FOUND, HttpStatus.BAD_REQUEST, "diet.ai.unit-unresolved",
                defaultString(label));
    }

    private UUID parseUuid(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return UUID.fromString(value.trim());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private JMException buildException(ProblemType type, HttpStatus status, String messageKey, Object... args) {
        Locale locale = LocaleContextHolder.getLocale();
        String message = messageSource.getMessage(messageKey, args, messageKey, locale);
        return new JMException(status.value(), type.getTitle(), type.getUri(), message);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record DietPlanPayload(
            String patientName,
            String notes,
            Boolean active,
            @JsonAlias({ "dayOfWeek", "day_of_week" }) String dayOfWeek,
            @JsonAlias({ "meals" }) List<MealPayload> meals) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record MealPayload(
            @JsonAlias({ "mealType", "meal_type" }) String mealType,
            @JsonAlias({ "scheduledTime", "scheduled_time", "time" }) String scheduledTime,
            @JsonAlias({ "items", "foods" }) List<ItemPayload> items) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ItemPayload(
            @JsonAlias({ "foodId", "food_id" }) String foodId,
            @JsonAlias({ "foodCode", "food_code" }) String foodCode,
            @JsonAlias({ "foodName", "food_name", "food" }) String foodName,
            @JsonAlias({ "unitId", "unit_id" }) String unitId,
            @JsonAlias({ "unitCode", "unit_code" }) String unitCode,
            @JsonAlias({ "unitName", "unit_name", "unit" }) String unitName,
            BigDecimal quantity,
            String notes) {
    }

    private record AiPromptContext(AiProvider provider, String model, String promptTemplate) {
    }
}
