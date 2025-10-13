package com.jm.services;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jm.dto.ImageDTO;
import com.jm.dto.NutritionDashboardDTO;
import com.jm.dto.OllamaRequestDTO;
import com.jm.dto.OllamaRequestDTO.OllamaRequestDTOBuilder;
import com.jm.dto.UserDTO;
import com.jm.dto.WhatsAppMediaMetadata;
import com.jm.dto.WhatsAppMessageDTO;
import com.jm.dto.WhatsAppMessageFeedDTO;
import com.jm.dto.WhatsAppNutritionEntryRequest;
import com.jm.entity.Food;
import com.jm.entity.FoodCategory;
import com.jm.entity.Meal;
import com.jm.entity.MeasurementUnits;
import com.jm.entity.NutritionAnalysis;
import com.jm.entity.Users;
import com.jm.entity.WhatsAppMessage;
import com.jm.repository.FoodCategoryRepository;
import com.jm.repository.FoodRepository;
import com.jm.repository.MealRepository;
import com.jm.repository.MeasurementUnitRepository;
import com.jm.repository.NutritionAnalysisRepository;
import com.jm.repository.WhatsAppMessageRepository;
import com.jm.speciation.WhatsAppSpecification;

import com.jm.execption.JMException;
import com.jm.execption.ProblemType;
import com.jm.utils.SecurityUtils;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WhatsAppNutritionService {

    private static final Logger logger = LoggerFactory.getLogger(WhatsAppNutritionService.class);
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);
    private static final ZoneId DEFAULT_ZONE = ZoneId.systemDefault();

    private final WhatsAppService whatsAppService;
    private final GeminiService geminiService;
    private final WhatsAppMessageRepository messageRepository;
    private final NutritionAnalysisRepository nutritionAnalysisRepository;
    private final FoodCategoryRepository foodCategoryRepository;
    private final MealRepository mealRepository;
    private final FoodRepository foodRepository;
    private final MeasurementUnitRepository measurementUnitRepository;
    private final ObjectMapper objectMapper;
    private final CloudflareR2Service cloudflareR2Service;
    private final UserService userService;
    private final OllamaService ollamaService;
    private static final String prompt = """
            You are a nutritionist and must reply using JSON only.
            Analyse the meal in the image and respond ONLY with this JSON structure:
            {
              "isFood": true|false,
              "foodName": "name of the meal",
              "calories": number in kcal,
              "mealType": "BREAKFAST|LUNCH|DINNER|SNACK|SUPPER|OTHER_MEALS",
              "macronutrients": {
                "protein_g": number in grams,
                "carbs_g": number in grams,
                "fat_g": number in grams
              },
              "categories": [
                { "name": "category name", "confidence": value between 0 and 1 }
              ],
              "summary": "short sentence about the meal",
              "confidence": value between 0 and 1
            }
            If the picture does not contain food, respond exactly with:
            {"isFood": false, "summary": "brief explanation"}
            Always choose the mealType that best matches the image. Use OTHER_MEALS when unsure.
            """;

    @SuppressWarnings("unchecked")
    @Transactional
    public void handleWebhook(Map<String, Object> payload) {
        List<Map<String, Object>> entries = (List<Map<String, Object>>) payload.getOrDefault("entry", List.of());
        if (entries.isEmpty()) {
            logger.warn("WhatsApp webhook received without entries");
            return;
        }

        Map<String, Object> entry = entries.get(0);
        List<Map<String, Object>> changes = (List<Map<String, Object>>) entry.getOrDefault("changes", List.of());
        if (changes.isEmpty()) {
            logger.warn("WhatsApp webhook received without changes");
            return;
        }

        Map<String, Object> value = (Map<String, Object>) changes.get(0).get("value");
        if (value == null) {
            logger.warn("WhatsApp webhook payload missing 'value'");
            return;
        }

        List<Map<String, Object>> messages = (List<Map<String, Object>>) value.getOrDefault("messages", List.of());
        if (messages.isEmpty()) {
            logger.info("WhatsApp webhook with no messages - likely an acknowledgment");
            return;
        }

        Map<String, Object> message = messages.get(0);
        String messageType = (String) message.getOrDefault("type", "text");
        String messageId = (String) message.get("id");
        if (messageId == null) {
            logger.warn("WhatsApp message without id; skipping");
            return;
        }

        if (messageRepository.findByWhatsappMessageId(messageId).isPresent()) {
            logger.debug("Skipping already processed message {}", messageId);
            return;
        }

        String from = (String) message.get("from");
        Map<String, Object> metadata = (Map<String, Object>) value.getOrDefault("metadata", Map.of());
        String to = (String) metadata.getOrDefault("display_phone_number", "");
        OffsetDateTime receivedAt = parseTimestamp((String) message.get("timestamp"));

        WhatsAppMessage.WhatsAppMessageBuilder builder = WhatsAppMessage.builder().whatsappMessageId(messageId)
                .fromPhone(from)
                .toPhone(to)
                .messageType(messageType)
                .receivedAt(receivedAt);

        switch (messageType) {
            case "image" -> handleImageMessage(builder, message, from);
            case "text" -> handleTextMessage(builder, message, from);
            default -> logger.info("Message type {} not handled", messageType);
        }
    }

    private void handleTextMessage(WhatsAppMessage.WhatsAppMessageBuilder builder, Map<String, Object> message,
            String from) {
        Map<String, Object> textContent = (Map<String, Object>) message.getOrDefault("text", Map.of());

        createAndDispatchRequest(builder, from, textContent);
        messageRepository.save(builder.build());
    }

    private void createAndDispatchRequest(WhatsAppMessage.WhatsAppMessageBuilder builder, String from,
            Map<String, Object> textContent) {
        OllamaRequestDTOBuilder ollamaRequestDTO = OllamaRequestDTO.builder();
        ollamaRequestDTO.from(from);
        ollamaRequestDTO.model("ALIENTELLIGENCE/personalizednutrition:latest");
        ollamaRequestDTO.prompt((String) textContent.getOrDefault("body", ""));
        ollamaRequestDTO.stream(Boolean.FALSE);
        Optional<Users> owner = findUserByPhone(from);
        if (owner.isPresent()) {
            ollamaRequestDTO.userId(owner.get().getId());
            builder.owner(owner.get());
        }
        ollamaService.createAndDispatchRequest(ollamaRequestDTO.build());
        builder.textContent((String) textContent.getOrDefault("body", ""));
    }

    private void handleImageMessage(WhatsAppMessage.WhatsAppMessageBuilder builder, Map<String, Object> message,
            String from) {
        Map<String, Object> image = (Map<String, Object>) message.getOrDefault("image", Map.of());
        String mediaId = (String) image.get("id");
        String caption = (String) image.getOrDefault("caption", "");

        builder.mediaId(mediaId);
        builder.textContent(caption);
        findUserByPhone(from).ifPresent(builder::owner);

        WhatsAppMessage savedMessage = messageRepository.save(builder.build());
        if (mediaId == null) {
            logger.warn("Image message without media id");
            return;
        }

        try {
            WhatsAppMediaMetadata metadata = whatsAppService.fetchMediaMetadata(mediaId).blockOptional(DEFAULT_TIMEOUT)
                    .orElseThrow(() -> new IllegalStateException("Unable to fetch media metadata"));

            byte[] imageBytes = whatsAppService.downloadMedia(metadata.getUrl()).blockOptional(DEFAULT_TIMEOUT)
                    .orElseThrow(() -> new IllegalStateException("Unable to download media"));

            MultipartFile imageFile = cloudflareR2Service.toMultipartFile(imageBytes, mediaId + ".jpg",
                    metadata.getMimeType());

            Users owner = Optional.ofNullable(savedMessage.getOwner())
                    .or(() -> findUserByPhone(savedMessage.getFromPhone()))
                    .orElse(null);

            savedMessage.setMediaUrl(metadata.getUrl());
            savedMessage.setMimeType(metadata.getMimeType());

            if (owner != null) {
                ImageDTO cloudflareImage = cloudflareR2Service.uploadFile(imageFile, owner.getId());
                savedMessage.setOwner(owner);
                savedMessage.setCloudflareImageUrl(cloudflareImage.getUrl());
            } else {
                logger.warn("Skipping Cloudflare upload for message {} because owner could not be resolved",
                        savedMessage.getId());
            }

            messageRepository.save(savedMessage);

            OllamaRequestDTO dto = OllamaRequestDTO.builder()
                    .from(from)
                    .model("llava-llama3:latest")
                    .userId(owner.getId())
                    .prompt(prompt)
                    .stream(Boolean.FALSE)
                    .images(ollamaService.encodeImages(Collections.singletonList(imageFile)))
                    .build();

            ollamaService.createAndDispatchRequest(dto);

            GeminiNutritionResult result = requestNutritionAnalysis(imageBytes, metadata.getMimeType(), prompt);
            if (result == null) {
                logger.warn("Gemini returned empty result for message {}", savedMessage.getId());
                return;
            }

            if (!result.isFood) {
                String response = Optional.ofNullable(result.summary)
                        .orElse("I could not detect food in this image. Please try another photo.");
                whatsAppService.sendTextMessage(from, response).subscribe();
                return;
            }

            saveNutritionAnalysis(savedMessage, result);
            whatsAppService.sendTextMessage(from, buildNutritionResponse(result)).subscribe();
        } catch (Exception ex) {
            logger.error("Failed to process WhatsApp image message", ex);
            whatsAppService.sendTextMessage(from, "Sorry, I could not analyse this image now. Please try again later.")
                    .subscribe();
        }
    }

    private Optional<Users> findUserByPhone(String phone) {
        if (!StringUtils.hasText(phone)) {
            return Optional.empty();
        }
        try {
            return userService
                    .findAll(PageRequest.of(0, 1), UserDTO.builder().phoneNumber(phone).build())
                    .getContent()
                    .stream()
                    .findFirst()
                    .map(dto -> {
                        try {
                            return userService.findEntityById(dto.getId());
                        } catch (JMException ex) {
                            logger.debug("Failed to load user {} for phone {}", dto.getId(), phone, ex);
                            return null;
                        }
                    })
                    .filter(Objects::nonNull);
        } catch (JMException ex) {
            logger.debug("Failed to resolve user by phone {}", phone, ex);
            return Optional.empty();
        }
    }

    public void saveNutritionAnalysis(WhatsAppMessage message, GeminiNutritionResult result)
            throws JsonProcessingException {
        MeasurementUnits caloriesUnit = resolveMeasurementUnit(null, "KCAL");
        MeasurementUnits macroUnit = resolveMeasurementUnit(null, "G");

        Meal detectedMeal = resolveMealFromResult(result);

        NutritionAnalysis analysis = NutritionAnalysis.builder()
                .message(message)
                .foodName(result.foodName)
                .calories(toBigDecimal(result.calories))
                .protein(toBigDecimal(result.macronutrients != null ? result.macronutrients.protein_g : null))
                .carbs(toBigDecimal(result.macronutrients != null ? result.macronutrients.carbs_g : null))
                .fat(toBigDecimal(result.macronutrients != null ? result.macronutrients.fat_g : null))
                .summary(result.summary)
                .categoriesJson(objectMapper.writeValueAsString(result.categories))
                .confidence(toBigDecimal(result.confidence))
                .primaryCategory(resolvePrimaryCategory(result.categories))
                .caloriesUnit(caloriesUnit)
                .proteinUnit(macroUnit)
                .carbsUnit(macroUnit)
                .fatUnit(macroUnit)
                .meal(detectedMeal)
                .build();

        message.setNutritionAnalysis(analysis);
        nutritionAnalysisRepository.save(analysis);
    }

    private FoodCategory resolvePrimaryCategory(List<GeminiNutritionResult.Category> categories) {
        if (categories == null || categories.isEmpty()) {
            return null;
        }
        GeminiNutritionResult.Category best = categories.stream().filter(Objects::nonNull)
                .max(Comparator.comparing(c -> Optional.ofNullable(c.confidence).orElse(0.0))).orElse(null);
        if (best == null || best.name == null || best.name.isBlank()) {
            return null;
        }
        String normalizedName = normalizeCategoryName(best.name);
        return foodCategoryRepository
                .findByNameIgnoreCase(normalizedName)
                .orElseGet(() -> foodCategoryRepository.save(FoodCategory.builder()
                        .name(normalizedName.substring(0, 1).toUpperCase() + normalizedName.substring(1))
                        .description("Categoria identificada automaticamente").build()));
    }

    public GeminiNutritionResult requestNutritionAnalysis(byte[] imageBytes, String mimeType, String prompt) {
        return geminiService.generateTextFromImage(prompt, imageBytes, mimeType).map(this::sanitizeGeminiResponse)
                .map(this::deserializeNutritionResult).blockOptional(DEFAULT_TIMEOUT).orElse(null);
    }

    public GeminiNutritionResult deserializeNutritionResult(String payload) {
        try {
            ObjectMapper mapper = objectMapper.copy().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                    false);
            return mapper.readValue(payload, GeminiNutritionResult.class);
        } catch (JsonProcessingException e) {
            logger.error("Failed to parse Gemini nutrition response: {}", payload, e);
            return null;
        }
    }

    private String sanitizeGeminiResponse(String raw) {
        if (raw == null) {
            return null;
        }
        String cleaned = raw.trim();
        cleaned = cleaned.replace("```json", "").replace("```", "").trim();
        return cleaned;
    }

    private BigDecimal toBigDecimal(Double value) {
        return value == null ? null : BigDecimal.valueOf(value);
    }

    private OffsetDateTime parseTimestamp(String timestamp) {
        try {
            if (timestamp == null) {
                return OffsetDateTime.now();
            }
            long seconds = Long.parseLong(timestamp);
            return Instant.ofEpochSecond(seconds).atOffset(ZoneOffset.UTC);
        } catch (NumberFormatException ex) {
            return OffsetDateTime.now();
        }
    }

    public String buildNutritionResponse(GeminiNutritionResult result) {
        String primaryCategory = result.categories != null && !result.categories.isEmpty()
                ? result.categories.get(0).name
                : "Alimento";
        String foodName = Optional.ofNullable(result.foodName).filter(s -> !s.isBlank()).orElse(primaryCategory);
        String mealLabel = formatMealTypeLabel(result.mealType());

        return """
                [Analysis Nutricional]
                _%s_

                Calorias: %.0f kcal
                Proteins: %.1f g
                Carboidratos: %.1f g
                Gorduras: %.1f g
                Categoria: %s
                Refeição: %s

                %s
                """.formatted(foodName, Optional.ofNullable(result.calories).orElse(0.0),
                result.macronutrients != null && result.macronutrients.protein_g != null
                        ? result.macronutrients.protein_g
                        : 0.0,
                result.macronutrients != null && result.macronutrients.carbs_g != null ? result.macronutrients.carbs_g
                        : 0.0,
                result.macronutrients != null && result.macronutrients.fat_g != null ? result.macronutrients.fat_g
                        : 0.0,
                primaryCategory, mealLabel,
                Optional.ofNullable(result.summary).orElse("Aproveite a sua meal!"));
    }

    private String normalizeCategoryName(String name) {
        String normalized = Normalizer.normalize(name, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return normalized.toLowerCase(Locale.ROOT).trim();
    }

    @Transactional(readOnly = true)
    public List<WhatsAppMessageFeedDTO> getRecentMessages() {
        List<WhatsAppMessage> messages = messageRepository.findTop20ByOrderByReceivedAtDesc();
        return messages.stream().map(this::toFeedDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<WhatsAppMessageFeedDTO> getRecentMessagesWithFilter(WhatsAppMessageDTO filter, UUID userId,
            LocalDate date) {
        OffsetDateTime start = startOfDay(date);
        OffsetDateTime end = endOfDayExclusive(date);

        List<WhatsAppMessage> messages = messageRepository.findAll(WhatsAppSpecification.search(filter, start, end));
        Optional<String> normalizedPhone = resolveUserPhone(userId);

        Comparator<WhatsAppMessage> comparator = Comparator
                .comparing(WhatsAppMessage::getReceivedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed();

        return messages.stream()
                .filter(message -> date == null || matchesDate(message.getReceivedAt(), date))
                .filter(message -> {
                    if (userId == null) {
                        return true;
                    }
                    UUID ownerId = Optional.ofNullable(message.getOwner()).map(Users::getId).orElse(null);
                    if (ownerId != null) {
                        return ownerId.equals(userId);
                    }
                    return normalizedPhone.map(expected -> phoneMatches(expected, message.getFromPhone())
                            || phoneMatches(expected, message.getToPhone())).orElse(false);
                })
                .sorted(comparator)
                .limit(50)
                .map(this::toFeedDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public WhatsAppMessageFeedDTO createManualEntry(WhatsAppNutritionEntryRequest request) {
        PermissionContext context = currentPermission();
        Users owner = resolveOwner(context, request.getOwnerUserId());

        WhatsAppMessage message = WhatsAppMessage.builder()
                .owner(owner)
                .manualEntry(true)
                .messageType("MANUAL")
                .fromPhone(owner.getPhoneNumber())
                .toPhone(owner.getPhoneNumber())
                .textContent(request.getTextContent())
                .cloudflareImageUrl(request.getImageUrl())
                .mediaUrl(request.getImageUrl())
                .receivedAt(Optional.ofNullable(request.getReceivedAt()).orElse(OffsetDateTime.now()))
                .build();

        NutritionAnalysis analysis = applyNutritionRequest(message, null, request);
        message.setNutritionAnalysis(analysis);

        WhatsAppMessage saved = messageRepository.save(message);
        return toFeedDto(saved);
    }

    @Transactional
    public WhatsAppMessageFeedDTO updateEntry(UUID messageId, WhatsAppNutritionEntryRequest request) {
        PermissionContext context = currentPermission();
        WhatsAppMessage message = messageRepository.findById(messageId)
                .orElseThrow(() -> messageNotFound(messageId));

        ensureCanManage(context, message);

        if (context.admin() && request.getOwnerUserId() != null) {
            Users newOwner = resolveOwner(context, request.getOwnerUserId());
            message.setOwner(newOwner);
            message.setFromPhone(newOwner.getPhoneNumber());
        } else if (!context.admin() && message.getOwner() == null) {
            Users owner = resolveOwner(context, context.userId());
            message.setOwner(owner);
        }

        if (request.getReceivedAt() != null) {
            message.setReceivedAt(request.getReceivedAt());
        }

        message.setTextContent(request.getTextContent());
        message.setCloudflareImageUrl(request.getImageUrl());
        message.setMediaUrl(request.getImageUrl());
        message.setEditedEntry(true);

        if (!StringUtils.hasText(message.getMessageType()) || "MANUAL".equalsIgnoreCase(message.getMessageType())) {
            message.setMessageType("MANUAL");
        }

        NutritionAnalysis analysis = applyNutritionRequest(message, message.getNutritionAnalysis(), request);
        message.setNutritionAnalysis(analysis);

        WhatsAppMessage saved = messageRepository.save(message);
        return toFeedDto(saved);
    }

    @Transactional
    public void deleteEntry(UUID messageId) {
        PermissionContext context = currentPermission();
        WhatsAppMessage message = messageRepository.findById(messageId)
                .orElseThrow(() -> messageNotFound(messageId));

        ensureCanManage(context, message);
        messageRepository.delete(message);
    }

    private WhatsAppMessageFeedDTO toFeedDto(WhatsAppMessage message) {
        NutritionAnalysis analysis = message.getNutritionAnalysis();
        WhatsAppMessageFeedDTO.NutritionSummary nutritionSummary = null;
        WhatsAppMessageFeedDTO.MealSummary mealSummary = null;
        WhatsAppMessageFeedDTO.FoodSummary foodSummary = null;
        WhatsAppMessageFeedDTO.LiquidSummary liquidSummary = null;

        if (analysis != null) {
            nutritionSummary = WhatsAppMessageFeedDTO.NutritionSummary
                    .builder()
                    .foodId(Optional.ofNullable(analysis.getFood()).map(Food::getId).orElse(null))
                    .foodName(analysis.getFoodName())
                    .calories(optionalDouble(analysis.getCalories()))
                    .protein(optionalDouble(analysis.getProtein()))
                    .carbs(optionalDouble(analysis.getCarbs()))
                    .fat(optionalDouble(analysis.getFat()))
                    .caloriesUnitId(
                            Optional.ofNullable(analysis.getCaloriesUnit()).map(MeasurementUnits::getId).orElse(null))
                    .caloriesUnitSymbol(Optional.ofNullable(analysis.getCaloriesUnit()).map(MeasurementUnits::getSymbol)
                            .orElse(null))
                    .proteinUnitId(
                            Optional.ofNullable(analysis.getProteinUnit()).map(MeasurementUnits::getId).orElse(null))
                    .proteinUnitSymbol(Optional.ofNullable(analysis.getProteinUnit()).map(MeasurementUnits::getSymbol)
                            .orElse(null))
                    .carbsUnitId(Optional.ofNullable(analysis.getCarbsUnit()).map(MeasurementUnits::getId).orElse(null))
                    .carbsUnitSymbol(
                            Optional.ofNullable(analysis.getCarbsUnit()).map(MeasurementUnits::getSymbol).orElse(null))
                    .fatUnitId(Optional.ofNullable(analysis.getFatUnit()).map(MeasurementUnits::getId).orElse(null))
                    .fatUnitSymbol(
                            Optional.ofNullable(analysis.getFatUnit()).map(MeasurementUnits::getSymbol).orElse(null))
                    .primaryCategoryId(
                            Optional.ofNullable(analysis.getPrimaryCategory()).map(FoodCategory::getId).orElse(null))
                    .primaryCategory(
                            Optional.ofNullable(analysis.getPrimaryCategory()).map(FoodCategory::getName).orElse(null))
                    .summary(analysis.getSummary())
                    .build();

            Meal meal = analysis.getMeal();
            if (meal != null) {
                mealSummary = WhatsAppMessageFeedDTO.MealSummary.builder()
                        .id(meal.getId())
                        .name(meal.getName())
                        .code(meal.getCode())
                        .description(meal.getDescription())
                        .sortOrder(meal.getSortOrder())
                        .build();
            }

            Food food = analysis.getFood();
            if (food != null) {
                foodSummary = WhatsAppMessageFeedDTO.FoodSummary.builder()
                        .id(food.getId())
                        .name(food.getName())
                        .code(food.getCode())
                        .categoryId(Optional.ofNullable(food.getFoodCategory()).map(FoodCategory::getId).orElse(null))
                        .categoryName(
                                Optional.ofNullable(food.getFoodCategory()).map(FoodCategory::getName).orElse(null))
                        .build();
            }

            BigDecimal liquidVolume = analysis.getLiquidVolume();
            MeasurementUnits liquidUnit = analysis.getLiquidUnit();
            if (liquidVolume != null && liquidVolume.signum() > 0) {
                liquidSummary = WhatsAppMessageFeedDTO.LiquidSummary.builder()
                        .volume(optionalDouble(liquidVolume))
                        .unitId(Optional.ofNullable(liquidUnit).map(MeasurementUnits::getId).orElse(null))
                        .unitSymbol(Optional.ofNullable(liquidUnit).map(MeasurementUnits::getSymbol).orElse(null))
                        .volumeMl(convertToMilliliters(liquidVolume, liquidUnit))
                        .build();
            }
        }

        String imageUrl = Optional.ofNullable(message.getCloudflareImageUrl())
                .filter(StringUtils::hasText)
                .orElseGet(() -> StringUtils.hasText(message.getMediaUrl()) ? message.getMediaUrl() : null);

        return WhatsAppMessageFeedDTO.builder()
                .id(message.getId())
                .whatsappMessageId(message.getWhatsappMessageId())
                .fromPhone(message.getFromPhone())
                .toPhone(message.getToPhone())
                .messageType(message.getMessageType())
                .textContent(message.getTextContent())
                .imageUrl(imageUrl)
                .receivedAt(message.getReceivedAt())
                .cloudFlareImageUrl(message.getCloudflareImageUrl())
                .manualEntry(message.isManualEntry())
                .editedEntry(message.isEditedEntry())
                .ownerUserId(Optional.ofNullable(message.getOwner()).map(Users::getId).orElse(null))
                .meal(mealSummary)
                .food(foodSummary)
                .liquids(liquidSummary)
                .nutrition(nutritionSummary)
                .build();
    }

    private NutritionAnalysis applyNutritionRequest(WhatsAppMessage message, NutritionAnalysis existing,
            WhatsAppNutritionEntryRequest request) {
        NutritionAnalysis target = existing != null ? existing : new NutritionAnalysis();
        target.setMessage(message);

        Meal meal = resolveMeal(request.getMealId());
        target.setMeal(meal);

        Food food = resolveFood(request.getFoodId());
        if (food != null) {
            target.setFood(food);
            String requestedName = StringUtils.hasText(request.getFoodName()) ? request.getFoodName() : null;
            target.setFoodName(requestedName != null ? requestedName : food.getName());
        } else {
            target.setFood(null);
            target.setFoodName(request.getFoodName());
        }

        target.setCalories(toBigDecimal(request.getCalories()));
        target.setProtein(toBigDecimal(request.getProtein()));
        target.setCarbs(toBigDecimal(request.getCarbs()));
        target.setFat(toBigDecimal(request.getFat()));
        target.setSummary(request.getSummary());

        target.setCaloriesUnit(resolveMeasurementUnit(request.getCaloriesUnitId(), "KCAL"));
        target.setProteinUnit(resolveMeasurementUnit(request.getProteinUnitId(), "G"));
        target.setCarbsUnit(resolveMeasurementUnit(request.getCarbsUnitId(), "G"));
        target.setFatUnit(resolveMeasurementUnit(request.getFatUnitId(), "G"));
        target.setPrimaryCategory(lookupFoodCategory(request.getPrimaryCategoryId()));

        target.setLiquidVolume(toBigDecimal(request.getLiquidVolume()));
        target.setLiquidUnit(resolveMeasurementUnit(request.getLiquidUnitId(), "ML"));

        if (existing != null) {
            target.setCategoriesJson(existing.getCategoriesJson());
            target.setConfidence(existing.getConfidence());
        } else {
            target.setCategoriesJson(null);
            target.setConfidence(null);
        }

        return target;
    }

    private MeasurementUnits resolveMeasurementUnit(UUID unitId, String fallbackCode) {
        if (unitId != null) {
            return measurementUnitRepository.findById(unitId)
                    .orElseThrow(
                            () -> new JMException(HttpStatus.BAD_REQUEST.value(), ProblemType.INVALID_DATA.getUri(),
                                    ProblemType.INVALID_DATA.getTitle(), "Measurement unit not found"));
        }
        if (!StringUtils.hasText(fallbackCode)) {
            return null;
        }
        return measurementUnitRepository.findByCodeIgnoreCase(fallbackCode).orElseGet(() -> {
            logger.warn("Measurement unit with code {} not found", fallbackCode);
            return null;
        });
    }

    private Meal resolveMeal(UUID mealId) {
        if (mealId == null) {
            return null;
        }
        return mealRepository.findById(mealId)
                .orElseThrow(() -> new JMException(HttpStatus.BAD_REQUEST.value(), ProblemType.INVALID_DATA.getUri(),
                        ProblemType.INVALID_DATA.getTitle(), "Meal not found"));
    }

    private Meal resolveMealFromResult(GeminiNutritionResult result) {
        if (result == null) {
            return resolveOtherMeal();
        }
        Meal resolved = resolveMealByType(result.mealType());
        return resolved != null ? resolved : resolveOtherMeal();
    }

    private Meal resolveMealByType(String mealType) {
        if (!StringUtils.hasText(mealType)) {
            return null;
        }
        String trimmed = mealType.trim();
        Meal direct = mealRepository.findByCodeIgnoreCase(trimmed)
                .or(() -> mealRepository.findByCodeIgnoreCase(trimmed.replace(' ', '_')))
                .or(() -> mealRepository.findByNameIgnoreCase(trimmed))
                .orElse(null);
        if (direct != null) {
            return direct;
        }

        String normalized = normalizeMealKey(trimmed);
        if (!StringUtils.hasText(normalized)) {
            return null;
        }

        List<Meal> meals = mealRepository.findAll();
        Optional<Meal> exact = meals.stream()
                .filter(meal -> normalizeMealKey(meal.getCode()).equals(normalized)
                        || normalizeMealKey(meal.getName()).equals(normalized))
                .findFirst();
        if (exact.isPresent()) {
            return exact.get();
        }

        String aliasCode = mapMealAlias(normalized);
        if (!StringUtils.hasText(aliasCode)) {
            return null;
        }
        return mealRepository.findByCodeIgnoreCase(aliasCode)
                .or(() -> meals.stream()
                        .filter(meal -> normalizeMealKey(meal.getCode()).equals(normalizeMealKey(aliasCode)))
                        .findFirst())
                .orElse(null);
    }

    private String normalizeMealKey(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return normalized.replaceAll("[^A-Za-z]", "").toUpperCase(Locale.ROOT);
    }

    private String mapMealAlias(String normalized) {
        return switch (normalized) {
            case "BREAKFAST", "CAFEDAMANHA", "CAFE", "MORNINGMEAL", "MORNINGSNACK" -> "BREAKFAST";
            case "LUNCH", "ALMOCO" -> "LUNCH";
            case "DINNER", "JANTAR" -> "DINNER";
            case "SUPPER", "CEIA" -> "SUPPER";
            case "SNACK", "LANCHE", "AFTERNOONSNACK", "BRUNCH", "EVENINGSNACK",
                    "LANCHEDATARDE", "LANCHEDAMANHA", "LANCHEDANOITE" ->
                "SNACK";
            default -> "OTHER_MEALS";
        };
    }

    private Meal resolveOtherMeal() {
        return mealRepository.findByCodeIgnoreCase("OTHER_MEALS")
                .or(() -> mealRepository.findByCodeIgnoreCase("OTHER"))
                .or(() -> mealRepository.findByNameIgnoreCase("Other meals"))
                .or(() -> mealRepository.findByNameIgnoreCase("Outras refeições"))
                .orElse(null);
    }

    private String formatMealTypeLabel(String mealType) {
        if (!StringUtils.hasText(mealType)) {
            return "Outras refeições";
        }
        String normalized = mealType.trim().toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "BREAKFAST" -> "Café da manhã";
            case "LUNCH" -> "Almoço";
            case "DINNER" -> "Jantar";
            case "SUPPER" -> "Ceia";
            case "SNACK" -> "Lanche";
            case "OTHER_MEALS", "OTHER" -> "Outras refeições";
            default -> {
                String alias = mapMealAlias(normalizeMealKey(mealType));
                if (!StringUtils.hasText(alias) || alias.equalsIgnoreCase(normalized)) {
                    yield "Outras refeições";
                }
                yield formatMealTypeLabel(alias);
            }
        };
    }

    private Food resolveFood(UUID foodId) {
        if (foodId == null) {
            return null;
        }
        return foodRepository.findById(foodId)
                .orElseThrow(() -> new JMException(HttpStatus.BAD_REQUEST.value(), ProblemType.INVALID_DATA.getUri(),
                        ProblemType.INVALID_DATA.getTitle(), "Food not found"));
    }

    private FoodCategory lookupFoodCategory(UUID categoryId) {
        if (categoryId == null) {
            return null;
        }
        return foodCategoryRepository.findById(categoryId).orElse(null);
    }

    private PermissionContext currentPermission() {
        UUID userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(
                        () -> new JMException(HttpStatus.FORBIDDEN.value(), ProblemType.WHATSAPP_FORBIDDEN.getUri(),
                                ProblemType.WHATSAPP_FORBIDDEN.getTitle(), "Unable to resolve authenticated user"));
        return new PermissionContext(userId, SecurityUtils.hasRole("ADMIN"));
    }

    private Users resolveOwner(PermissionContext context, UUID ownerId) {
        if (context.admin() && ownerId == null) {
            throw new JMException(HttpStatus.BAD_REQUEST.value(), ProblemType.INVALID_DATA.getUri(),
                    ProblemType.INVALID_DATA.getTitle(), "Target user is required for this operation");
        }
        UUID targetId = ownerId != null ? ownerId : context.userId();
        if (targetId == null) {
            throw new JMException(HttpStatus.BAD_REQUEST.value(), ProblemType.INVALID_DATA.getUri(),
                    ProblemType.INVALID_DATA.getTitle(), "Owner user is required");
        }
        if (!context.admin() && !targetId.equals(context.userId())) {
            throw new JMException(HttpStatus.FORBIDDEN.value(), ProblemType.WHATSAPP_FORBIDDEN.getUri(),
                    ProblemType.WHATSAPP_FORBIDDEN.getTitle(),
                    "You are not allowed to manage nutrition entries for other users");
        }
        try {
            return userService.findEntityById(targetId);
        } catch (JMException ex) {
            throw ex;
        }
    }

    private void ensureCanManage(PermissionContext context, WhatsAppMessage message) {
        if (context.admin()) {
            return;
        }
        UUID ownerId = Optional.ofNullable(message.getOwner()).map(Users::getId).orElse(null);
        if (ownerId == null || !ownerId.equals(context.userId())) {
            throw new JMException(HttpStatus.FORBIDDEN.value(), ProblemType.WHATSAPP_FORBIDDEN.getUri(),
                    ProblemType.WHATSAPP_FORBIDDEN.getTitle(),
                    "You are not allowed to modify this nutrition entry");
        }
    }

    private JMException messageNotFound(UUID id) {
        return new JMException(HttpStatus.NOT_FOUND.value(), ProblemType.WHATSAPP_MESSAGE_NOT_FOUND.getUri(),
                ProblemType.WHATSAPP_MESSAGE_NOT_FOUND.getTitle(),
                "WhatsApp nutrition entry " + id + " not found");
    }

    private record PermissionContext(UUID userId, boolean admin) {
    }

    @Transactional(readOnly = true)
    public NutritionDashboardDTO getDashboard(UUID userId, LocalDate date) {
        OffsetDateTime start = startOfDay(date);
        OffsetDateTime endExclusive = endOfDayExclusive(date);

        List<NutritionAnalysis> analyses;
        if (start != null && endExclusive != null) {
            OffsetDateTime endInclusive = endExclusive.minusNanos(1);
            analyses = nutritionAnalysisRepository.findByCreatedAtBetween(start, endInclusive);
        } else {
            analyses = nutritionAnalysisRepository.findTop20ByOrderByCreatedAtDesc();
        }

        Optional<String> normalizedPhone = resolveUserPhone(userId);
        if (normalizedPhone.isPresent()) {
            String expected = normalizedPhone.get();
            analyses = analyses.stream()
                    .filter(analysis -> analysis.getMessage() != null
                            && phoneMatches(expected, analysis.getMessage().getFromPhone()))
                    .collect(Collectors.toList());
        }

        if (date != null) {
            analyses = analyses.stream().filter(analysis -> matchesAnalysisDate(analysis, date))
                    .collect(Collectors.toList());
        }

        double totalCalories = analyses.stream().mapToDouble(a -> optionalDouble(a.getCalories())).sum();
        double totalProtein = analyses.stream().mapToDouble(a -> optionalDouble(a.getProtein())).sum();
        double totalCarbs = analyses.stream().mapToDouble(a -> optionalDouble(a.getCarbs())).sum();
        double totalFat = analyses.stream().mapToDouble(a -> optionalDouble(a.getFat())).sum();
        double totalLiquidMl = analyses.stream()
                .mapToDouble(a -> convertToMilliliters(a.getLiquidVolume(), a.getLiquidUnit()))
                .sum();

        String liquidSymbol = totalLiquidMl >= 1000 ? "L" : "ml";
        double totalLiquidDisplay = "L".equals(liquidSymbol) ? totalLiquidMl / 1000d : totalLiquidMl;

        Map<String, Double> categoryCalories = new ConcurrentHashMap<>();
        analyses.forEach(analysis -> {
            String category = Optional.ofNullable(analysis.getPrimaryCategory()).map(FoodCategory::getName)
                    .orElse("Outros");
            categoryCalories.merge(category, optionalDouble(analysis.getCalories()), Double::sum);
        });

        List<NutritionDashboardDTO.NutritionHistoryItem> history = analyses.stream()
                .map(analysis -> NutritionDashboardDTO.NutritionHistoryItem.builder()
                        .messageId(analysis.getMessage().getId())
                        .foodId(Optional.ofNullable(analysis.getFood()).map(Food::getId).orElse(null))
                        .foodName(analysis.getFoodName())
                        .calories(optionalDouble(analysis.getCalories()))
                        .protein(optionalDouble(analysis.getProtein()))
                        .carbs(optionalDouble(analysis.getCarbs()))
                        .fat(optionalDouble(analysis.getFat()))
                        .liquidVolume(optionalDouble(analysis.getLiquidVolume()))
                        .liquidVolumeMl(convertToMilliliters(analysis.getLiquidVolume(), analysis.getLiquidUnit()))
                        .liquidUnitSymbol(Optional.ofNullable(analysis.getLiquidUnit()).map(MeasurementUnits::getSymbol)
                                .orElse(null))
                        .mealId(Optional.ofNullable(analysis.getMeal()).map(Meal::getId).orElse(null))
                        .mealName(Optional.ofNullable(analysis.getMeal()).map(Meal::getName).orElse(null))
                        .primaryCategory(Optional.ofNullable(analysis.getPrimaryCategory())
                                .map(FoodCategory::getName).orElse(null))
                        .analyzedAt(analysis.getCreatedAt())
                        .summary(analysis.getSummary())
                        .build())
                .collect(Collectors.toList());

        return NutritionDashboardDTO.builder()
                .totalCalories(totalCalories)
                .totalProtein(totalProtein)
                .totalCarbs(totalCarbs)
                .totalFat(totalFat)
                .totalLiquidVolume(totalLiquidDisplay)
                .totalLiquidVolumeMl(totalLiquidMl)
                .liquidUnitSymbol(liquidSymbol)
                .mealsAnalyzed(analyses.size())
                .categoryCalories(categoryCalories)
                .history(history)
                .build();
    }

    @Transactional(readOnly = true)
    public Optional<ImagePayload> loadImage(UUID messageId) {
        return findMessage(messageId).flatMap(message -> {
            if (!"image".equalsIgnoreCase(message.getMessageType()) || message.getMediaId() == null) {
                return Optional.empty();
            }
            try {
                WhatsAppMediaMetadata metadata = whatsAppService.fetchMediaMetadata(message.getMediaId())
                        .blockOptional(DEFAULT_TIMEOUT).orElse(null);
                String url = metadata != null ? metadata.getUrl() : message.getMediaUrl();
                if (url == null) {
                    return Optional.empty();
                }
                byte[] bytes = whatsAppService.downloadMedia(url).blockOptional(DEFAULT_TIMEOUT).orElse(null);
                if (bytes == null) {
                    return Optional.empty();
                }
                String mimeType = metadata != null ? metadata.getMimeType() : message.getMimeType();
                return Optional.of(new ImagePayload(bytes, mimeType));
            } catch (Exception ex) {
                logger.error("Failed to load WhatsApp media for message {}", messageId, ex);
                return Optional.empty();
            }
        });
    }

    public record ImagePayload(byte[] data, String mimeType) {
    }

    private double optionalDouble(BigDecimal value) {
        return value == null ? 0.0 : value.doubleValue();
    }

    private double convertToMilliliters(BigDecimal volume, MeasurementUnits unit) {
        if (volume == null || volume.signum() <= 0) {
            return 0.0;
        }
        double base = optionalDouble(volume);
        if (unit == null) {
            return base;
        }
        Double factor = unit.getConversionFactor();
        if (factor != null && factor > 0) {
            return base * factor;
        }
        String code = unit.getCode() != null ? unit.getCode().toUpperCase(Locale.ROOT) : "";
        return switch (code) {
            case "L", "LT", "LITRE", "LITER" -> base * 1000;
            case "CUP" -> base * 240;
            case "TBSP" -> base * 15;
            case "TSP" -> base * 5;
            default -> base;
        };
    }

    private Optional<String> resolveUserPhone(UUID userId) {
        if (userId == null) {
            return Optional.empty();
        }
        try {
            Users user = userService.findEntityById(userId);
            if (!StringUtils.hasText(user.getPhoneNumber())) {
                return Optional.empty();
            }
            return Optional.of(normalizePhone(user.getPhoneNumber()));
        } catch (JMException ex) {
            logger.warn("Unable to resolve user {} for nutrition assistant filter", userId, ex);
            return Optional.empty();
        }
    }

    private String normalizePhone(String phone) {
        return phone == null ? null : phone.replaceAll("\\D", "");
    }

    private boolean phoneMatches(String expected, String candidate) {
        if (!StringUtils.hasText(expected) || !StringUtils.hasText(candidate)) {
            return false;
        }
        String normalizedCandidate = normalizePhone(candidate);
        if (!StringUtils.hasText(normalizedCandidate)) {
            return false;
        }
        return normalizedCandidate.equals(expected) || normalizedCandidate.endsWith(expected);
    }

    private boolean matchesDate(OffsetDateTime timestamp, LocalDate date) {
        if (timestamp == null || date == null) {
            return false;
        }
        return timestamp.atZoneSameInstant(DEFAULT_ZONE).toLocalDate().isEqual(date);
    }

    private boolean matchesAnalysisDate(NutritionAnalysis analysis, LocalDate date) {
        if (analysis == null || date == null) {
            return false;
        }
        OffsetDateTime reference = Optional.ofNullable(analysis.getMessage())
                .map(WhatsAppMessage::getReceivedAt)
                .orElse(analysis.getCreatedAt());
        return matchesDate(reference, date);
    }

    private OffsetDateTime startOfDay(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.atStartOfDay(DEFAULT_ZONE).toOffsetDateTime();
    }

    private OffsetDateTime endOfDayExclusive(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.plusDays(1).atStartOfDay(DEFAULT_ZONE).toOffsetDateTime();
    }

    @Transactional(readOnly = true)
    public Optional<WhatsAppMessage> findMessage(UUID messageId) {
        return messageRepository.findById(messageId);
    }

    public record GeminiNutritionResult(boolean isFood, String foodName, Double calories, String mealType,
            Macronutrients macronutrients, List<Category> categories, String summary, Double confidence) {

        private record Macronutrients(@JsonProperty("protein_g") Double protein_g,
                @JsonProperty("carbs_g") Double carbs_g,
                @JsonProperty("fat_g") Double fat_g) {
        }

        private record Category(String name, Double confidence) {
        }
    }
}
