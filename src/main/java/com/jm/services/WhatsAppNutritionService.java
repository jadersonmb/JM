package com.jm.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jm.dto.ImageDTO;
import com.jm.dto.NutritionDashboardDTO;
import com.jm.dto.UserDTO;
import com.jm.dto.WhatsAppMediaMetadata;
import com.jm.dto.WhatsAppMessageDTO;
import com.jm.dto.WhatsAppMessageFeedDTO;
import com.jm.entity.FoodCategory;
import com.jm.entity.NutritionAnalysis;
import com.jm.entity.WhatsAppMessage;
import com.jm.repository.FoodCategoryRepository;
import com.jm.repository.NutritionAnalysisRepository;
import com.jm.repository.WhatsAppMessageRepository;
import com.jm.speciation.WhatsAppSpecification;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
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

    private final WhatsAppService whatsAppService;
    private final GeminiService geminiService;
    private final WhatsAppMessageRepository messageRepository;
    private final NutritionAnalysisRepository nutritionAnalysisRepository;
    private final FoodCategoryRepository foodCategoryRepository;
    private final ObjectMapper objectMapper;
    private final CloudflareR2Service cloudflareR2Service;
    private final UserService userService;

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
                .fromPhone(from).toPhone(to).messageType(messageType).receivedAt(receivedAt);

        switch (messageType) {
            case "image" -> handleImageMessage(builder, message, from);
            case "text" -> handleTextMessage(builder, message);
            default -> logger.info("Message type {} not handled", messageType);
        }
    }

    private void handleTextMessage(WhatsAppMessage.WhatsAppMessageBuilder builder, Map<String, Object> message) {
        Map<String, Object> textContent = (Map<String, Object>) message.getOrDefault("text", Map.of());
        builder.textContent((String) textContent.getOrDefault("body", ""));
        messageRepository.save(builder.build());
    }

    private void handleImageMessage(WhatsAppMessage.WhatsAppMessageBuilder builder, Map<String, Object> message,
            String from) {
        Map<String, Object> image = (Map<String, Object>) message.getOrDefault("image", Map.of());
        String mediaId = (String) image.get("id");
        String caption = (String) image.getOrDefault("caption", "");

        builder.mediaId(mediaId);
        builder.textContent(caption);

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
            ImageDTO cloudflareImage = cloudflareR2Service.uploadFile(imageFile, userService
                    .findAll(PageRequest.of(0, 20), UserDTO.builder().phoneNumber(savedMessage.getFromPhone()).build())
                    .getContent().getFirst().getId());

            savedMessage.setMediaUrl(metadata.getUrl());
            savedMessage.setMimeType(metadata.getMimeType());
            savedMessage.setCloudflareImageUrl(cloudflareImage.getUrl());
            messageRepository.save(savedMessage);

            GeminiNutritionResult result = requestNutritionAnalysis(imageBytes, metadata.getMimeType());
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

    private void saveNutritionAnalysis(WhatsAppMessage message, GeminiNutritionResult result)
            throws JsonProcessingException {
        NutritionAnalysis analysis = NutritionAnalysis.builder().message(message).foodName(result.foodName)
                .calories(toBigDecimal(result.calories))
                .protein(toBigDecimal(result.macronutrients != null ? result.macronutrients.protein_g : null))
                .carbs(toBigDecimal(result.macronutrients != null ? result.macronutrients.carbs_g : null))
                .fat(toBigDecimal(result.macronutrients != null ? result.macronutrients.fat_g : null))
                .summary(result.summary).categoriesJson(objectMapper.writeValueAsString(result.categories))
                .confidence(toBigDecimal(result.confidence)).primaryCategory(resolvePrimaryCategory(result.categories))
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

    private GeminiNutritionResult requestNutritionAnalysis(byte[] imageBytes, String mimeType) {
        String prompt = """
                You are a nutritionist and must reply using JSON only.
                Analyse the meal in the image and respond ONLY with this JSON structure:
                {
                  "isFood": true|false,
                  "foodName": "name of the meal",
                  "calories": number in kcal,
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
                """;

        return geminiService.generateTextFromImage(prompt, imageBytes, mimeType).map(this::sanitizeGeminiResponse)
                .map(this::deserializeNutritionResult).blockOptional(DEFAULT_TIMEOUT).orElse(null);
    }

    private GeminiNutritionResult deserializeNutritionResult(String payload) {
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

    private String buildNutritionResponse(GeminiNutritionResult result) {
        String primaryCategory = result.categories != null && !result.categories.isEmpty()
                ? result.categories.get(0).name
                : "Alimento";
        String foodName = Optional.ofNullable(result.foodName).filter(s -> !s.isBlank()).orElse(primaryCategory);

        return """
                [Analysis Nutricional]
                _%s_

                Calorias: %.0f kcal
                Proteins: %.1f g
                Carboidratos: %.1f g
                Gorduras: %.1f g
                Categoria: %s

                %s
                """.formatted(foodName, Optional.ofNullable(result.calories).orElse(0.0),
                result.macronutrients != null && result.macronutrients.protein_g != null
                        ? result.macronutrients.protein_g
                        : 0.0,
                result.macronutrients != null && result.macronutrients.carbs_g != null ? result.macronutrients.carbs_g
                        : 0.0,
                result.macronutrients != null && result.macronutrients.fat_g != null ? result.macronutrients.fat_g
                        : 0.0,
                primaryCategory, Optional.ofNullable(result.summary).orElse("Aproveite a sua meal!"));
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
    public List<WhatsAppMessageFeedDTO> getRecentMessagesWithFilter(WhatsAppMessageDTO filter) {
        List<WhatsAppMessage> messages = messageRepository.findAll(WhatsAppSpecification.search(filter));
        return messages.stream().map(this::toFeedDto).collect(Collectors.toList());
    }

    private WhatsAppMessageFeedDTO toFeedDto(WhatsAppMessage message) {
        NutritionAnalysis analysis = message.getNutritionAnalysis();
        WhatsAppMessageFeedDTO.NutritionSummary nutritionSummary = null;
        if (analysis != null) {
            nutritionSummary = WhatsAppMessageFeedDTO.NutritionSummary
                    .builder()
                    .foodName(analysis.getFoodName())
                    .calories(optionalDouble(analysis.getCalories()))
                    .protein(optionalDouble(analysis.getProtein()))
                    .carbs(optionalDouble(analysis.getCarbs()))
                    .fat(optionalDouble(analysis.getFat()))
                    .primaryCategory(
                            Optional.ofNullable(analysis.getPrimaryCategory()).map(FoodCategory::getName).orElse(null))
                    .summary(analysis.getSummary())
                    .build();
        }

        String imageUrl = null;
        if ("image".equalsIgnoreCase(message.getMessageType())) {
            imageUrl = message.getCloudflareImageUrl();

        }

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
                .nutrition(nutritionSummary)
                .build();
    }

    @Transactional(readOnly = true)
    public NutritionDashboardDTO getDashboard() {
        List<NutritionAnalysis> analyses = nutritionAnalysisRepository.findTop20ByOrderByCreatedAtDesc();
        double totalCalories = analyses.stream().mapToDouble(a -> optionalDouble(a.getCalories())).sum();
        double totalProtein = analyses.stream().mapToDouble(a -> optionalDouble(a.getProtein())).sum();
        double totalCarbs = analyses.stream().mapToDouble(a -> optionalDouble(a.getCarbs())).sum();
        double totalFat = analyses.stream().mapToDouble(a -> optionalDouble(a.getFat())).sum();

        Map<String, Double> categoryCalories = new ConcurrentHashMap<>();
        analyses.forEach(analysis -> {
            String category = Optional.ofNullable(analysis.getPrimaryCategory()).map(FoodCategory::getName)
                    .orElse("Outros");
            categoryCalories.merge(category, optionalDouble(analysis.getCalories()), Double::sum);
        });

        List<NutritionDashboardDTO.NutritionHistoryItem> history = analyses.stream()
                .map(analysis -> NutritionDashboardDTO.NutritionHistoryItem.builder()
                        .messageId(analysis.getMessage().getId())
                        .foodName(analysis.getFoodName())
                        .calories(optionalDouble(analysis.getCalories()))
                        .protein(optionalDouble(analysis.getProtein()))
                        .carbs(optionalDouble(analysis.getCarbs()))
                        .fat(optionalDouble(analysis.getFat()))
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

    @Transactional(readOnly = true)
    public Optional<WhatsAppMessage> findMessage(UUID messageId) {
        return messageRepository.findById(messageId);
    }

    private record GeminiNutritionResult(boolean isFood, String foodName, Double calories,
            Macronutrients macronutrients, List<Category> categories, String summary, Double confidence) {

        private record Macronutrients(@com.fasterxml.jackson.annotation.JsonProperty("protein_g") Double protein_g,
                @com.fasterxml.jackson.annotation.JsonProperty("carbs_g") Double carbs_g,
                @com.fasterxml.jackson.annotation.JsonProperty("fat_g") Double fat_g) {
        }

        private record Category(String name, Double confidence) {
        }
    }
}
