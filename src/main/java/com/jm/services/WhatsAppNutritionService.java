package com.jm.services;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jm.dto.AiPromptReferenceDTO;
import com.jm.dto.ImageDTO;
import com.jm.dto.NutritionDashboardDTO;
import com.jm.dto.OllamaRequestDTO;
import com.jm.dto.OllamaResponseDTO;
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
import com.jm.events.NutritionAnalysisRequestEvent;
import com.jm.repository.FoodCategoryRepository;
import com.jm.repository.FoodRepository;
import com.jm.repository.MealRepository;
import com.jm.repository.MeasurementUnitRepository;
import com.jm.repository.NutritionAnalysisRepository;
import com.jm.repository.WhatsAppMessageRepository;
import com.jm.speciation.WhatsAppSpecification;

import com.jm.enums.AiProvider;
import com.jm.execption.JMException;
import com.jm.execption.ProblemType;
import com.jm.services.WhatsAppCaptionTemplate;
import com.jm.services.ai.AiClient;
import com.jm.services.ai.AiClientFactory;
import com.jm.services.ai.AiRequest;
import com.jm.services.ai.AiRequestType;
import com.jm.services.ai.AiResponse;
import com.jm.utils.SecurityUtils;

import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WhatsAppNutritionService {

    private static final Logger logger = LoggerFactory.getLogger(WhatsAppNutritionService.class);
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);
    private static final ZoneId DEFAULT_ZONE = ZoneId.systemDefault();
    private static final String PROMPT_CODE = "WHATSAPP_NUTRITION_ANALYSIS";
    private static final String DEFAULT_ASSISTANT_MODEL = "ALIENTELLIGENCE/personalizednutrition:latest";
    private static final String DEFAULT_ANALYSIS_MODEL = "gemini-pro-vision";
    private static final String DEFAULT_OLLAMA_VISION_MODEL = "llava:34b";
    private static final String OLLAMA_AUDIO_MODEL = "nuextract:latest";
    private static final String DEFAULT_IMAGE_PROMPT = """
            You are a nutrition vision assistant. Analyse the meal image and respond ONLY with the following JSON structure:
            {
              \"isFood\": true|false,
              \"foodName\": \"name of the meal\",
              \"dish_name\": \"short dish name in Portuguese\",
              \"dish_emoji\": \"emoji that represents the dish\",
              \"meal_name\": \"Breakfast|Lunch|Dinner|Snack|Supper|Other meals\",
              \"mealType\": \"BREAKFAST|LUNCH|DINNER|SNACK|SUPPER|OTHER_MEALS\",
              \"portion\": number in grams or null,
              \"calories\": null,
              \"kcal\": null,
              \"macronutrients\": {
                \"protein_g\": null,
                \"carbs_g\": null,
                \"fat_g\": null,
                \"fiber_g\": null
              },
              \"items\": [
                { \"name\": \"food item\", \"confidence\": value between 0 and 1, \"portion_g\": number in grams or null }
              ],
              \"categories\": [
                { \"name\": \"category name\", \"confidence\": value between 0 and 1 }
              ],
              \"summary\": \"short sentence about the meal\",
              \"confidence\": value between 0 and 1
            }
            Focus on identifying the food items accurately. If the picture does not contain food, respond exactly with:
            {\"isFood\": false, \"summary\": \"brief explanation\"}
            Keep the response compact and valid JSON.
            """;
    private static final String DEFAULT_PERSONALIZED_PROMPT = """
            You are a personalised nutrition assistant. A vision model analysed a meal photo and produced the JSON below:
            {{DETECTION_JSON}}
            The detected items were: {{ITEM_NAMES}}.
            Using this information and your nutrition knowledge, estimate the missing nutritional values and respond ONLY with JSON following this schema:
            {
              \"isFood\": true|false,
              \"foodName\": \"name of the meal\",
              \"dish_name\": \"short dish name in Portuguese\",
              \"dish_emoji\": \"emoji that represents the dish\",
              \"meal_name\": \"Breakfast|Lunch|Dinner|Snack|Supper|Other meals\",
              \"mealType\": \"BREAKFAST|LUNCH|DINNER|SNACK|SUPPER|OTHER_MEALS\",
              \"portion\": number in grams or null,
              \"calories\": number in kcal or null,
              \"kcal\": number in kcal or null,
              \"macronutrients\": {
                \"protein_g\": number in grams or null,
                \"carbs_g\": number in grams or null,
                \"fat_g\": number in grams or null,
                \"fiber_g\": number in grams or null
              },
              \"items\": [
                { \"name\": \"food item\", \"confidence\": value between 0 and 1, \"portion_g\": number in grams or null }
              ],
              \"categories\": [
                { \"name\": \"category name\", \"confidence\": value between 0 and 1 }
              ],
              \"summary\": \"short sentence about the meal\",
              \"confidence\": value between 0 and 1
            }
            Keep the structure, item names and meal classification from the input. When you cannot estimate a value, keep it null.
            If the analysis indicates there is no food, respond exactly with {\"isFood\": false, \"summary\": \"brief explanation\"}.
            """;
    private static final String COMMAND_EXTRACTION_PROMPT = """
            You are an assistant that extracts structured nutrition instructions from WhatsApp messages in Portuguese or English.
            Analyse the user message and output ONLY a JSON object with this structure:
            {
              "action": "ADD|EDIT|DELETE|UNKNOWN",
              "meal": "BREAKFAST|LUNCH|DINNER|SNACK|SUPPER|OTHER_MEALS|null",
              "items": [
                 {
                   "food": "name of the food",
                   "quantity": number or null,
                   "unit": "textual unit (e.g. gramas, ml, unidade) or null",
                   "observation": "notes about this food or null"
                   "calories": number or null,
                   "protein": number or null,
                   "carbs": number or null,
                   "fat": number or null,
                   "fiber_g: number or null,
                   "Ingested calories: number or null,
                   "summary": "short sentence about this food or null"
                 }
              ],
              "observation": "general observation or null"
            }
            Use action UNKNOWN when there is no nutrition command.
            Keep the food names concise and in Portuguese when the request is in Portuguese.
            Do not include extra text outside the JSON.
            """;
    private final WhatsAppService whatsAppService;
    private final WhatsAppMessageRepository messageRepository;
    private final NutritionAnalysisRepository nutritionAnalysisRepository;
    private final FoodCategoryRepository foodCategoryRepository;
    private final MealRepository mealRepository;
    private final FoodRepository foodRepository;
    private final MeasurementUnitRepository measurementUnitRepository;
    private final ObjectMapper objectMapper;
    private final CloudflareR2Service cloudflareR2Service;
    private final UserService userService;
    private final AiClientFactory aiClientFactory;
    private final AiPromptReferenceService aiPromptReferenceService;
    private final OllamaService ollamaService;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${whatsapp.nutrition.ai.assistant-provider:OLLAMA}")
    private AiProvider assistantProvider;

    @Value("${whatsapp.nutrition.ai.assistant-model:ALIENTELLIGENCE/personalizednutrition:latest}")
    private String assistantModel;

    @Value("${whatsapp.nutrition.ai.command-provider:GEMINI}")
    private AiProvider commandProvider;

    @Value("${whatsapp.nutrition.ai.command-model:gemini-pro}")
    private String commandModel;

    @Value("${whatsapp.nutrition.ai.analysis-provider:GEMINI}")
    private AiProvider analysisProvider;

    @Value("${whatsapp.nutrition.ai.analysis-model:gemini-pro-vision}")
    private String analysisModel;

    @Value("${whatsapp.nutrition.ai.ollama-vision-model:llava:34b}")
    private String ollamaVisionModel;

    private final Map<String, MeasurementUnits> measurementUnitByKeyword = new ConcurrentHashMap<>();
    private final Map<String, Food> foodByNormalizedName = new ConcurrentHashMap<>();
    private volatile List<MeasurementUnits> cachedMeasurementUnits;
    private volatile Map<String, MeasurementUnits> measurementUnitAliasIndex;
    private volatile List<Food> cachedFoods;

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
            case "audio", "voice" -> handleAudioMessage(builder, message, from);
            default -> logger.info("Message type {} not handled", messageType);
        }
    }

    private void handleTextMessage(WhatsAppMessage.WhatsAppMessageBuilder builder, Map<String, Object> message,
            String from) {
        Map<String, Object> textContent = (Map<String, Object>) message.getOrDefault("text", Map.of());
        String body = (String) textContent.getOrDefault("body", "");

        builder.textContent(body);
        findUserByPhone(from).ifPresent(builder::owner);

        WhatsAppMessage savedMessage = messageRepository.save(builder.build());
        dispatchToAssistant(savedMessage, body);
        /* processCommandFromText(savedMessage, from); */
    }

    private void dispatchToAssistant(WhatsAppMessage message, String body) {
        if (!StringUtils.hasText(body)) {
            return;
        }

        AiModelSelection assistantConfig = resolveModelSelection(assistantModel, assistantProvider,
                AiProvider.OLLAMA, DEFAULT_ASSISTANT_MODEL);
        AiProvider provider = assistantConfig.provider();
        String model = assistantConfig.model();

        AiRequest.AiRequestBuilder requestBuilder = AiRequest.builder()
                .type(AiRequestType.TEXT)
                .model(model)
                .prompt(body)
                .stream(Boolean.FALSE)
                .from(message.getFromPhone());

        Optional.ofNullable(message.getOwner())
                .map(Users::getId)
                .ifPresent(requestBuilder::userId);

        resolveClient(provider).ifPresent(client -> client.execute(requestBuilder.build()));
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

            AiModelSelection analysisConfig = resolveModelSelection(analysisModel, analysisProvider,
                    AiProvider.GEMINI, DEFAULT_ANALYSIS_MODEL);

            AiProvider provider = analysisConfig.provider();
            eventPublisher.publishEvent(new NutritionAnalysisRequestEvent(this, savedMessage, imageBytes,
                    metadata.getMimeType(), owner, from, provider, analysisConfig.model()));
        } catch (Exception ex) {
            logger.error("Failed to process WhatsApp image message", ex);
            whatsAppService.sendTextMessage(from, "Sorry, I could not analyse this image now. Please try again later.")
                    .subscribe();
        }
    }

    public void processNutritionAnalysis(WhatsAppMessage savedMessage, byte[] imageBytes, String mimeType, Users owner,
            String from, AiProvider provider, String model) {
        if (savedMessage == null) {
            return;
        }

        AiProvider effectiveProvider = provider != null ? provider : AiProvider.GEMINI;
        AiModelSelection analysisConfig = new AiModelSelection(effectiveProvider, model);

        try {
            GeminiNutritionResult result = requestNutritionAnalysis(imageBytes, mimeType, owner, analysisConfig);
            if (result == null) {
                logger.warn("Nutrition provider {} returned empty result for message {}", effectiveProvider,
                        savedMessage.getId());
                return;
            }

            if (!result.isFood()) {
                String response = Optional.ofNullable(result.summary())
                        .orElse("I could not detect food in this image. Please try another photo.");
                if (StringUtils.hasText(from)) {
                    whatsAppService.sendTextMessage(from, response).subscribe();
                }
                return;
            }

            saveNutritionAnalysis(savedMessage, result);
            Map<String, Object> captionVariables = buildNutritionCaptionVariables(result, owner,
                    savedMessage.getReceivedAt());
            if (StringUtils.hasText(from)) {
                whatsAppService.sendCaptionMessage(from, WhatsAppCaptionTemplate.DAILY_EN, captionVariables)
                        .subscribe();
            }
        } catch (Exception ex) {
            logger.error("Failed to process nutrition analysis for message {}", savedMessage.getId(), ex);
            if (StringUtils.hasText(from)) {
                whatsAppService.sendTextMessage(from,
                        "Sorry, I could not analyse this image now. Please try again later.").subscribe();
            }
        }
    }

    private void handleAudioMessage(WhatsAppMessage.WhatsAppMessageBuilder builder, Map<String, Object> message,
            String from) {
        Map<String, Object> audio = (Map<String, Object>) message.getOrDefault("audio", Map.of());
        String mediaId = (String) audio.get("id");
        String mimeType = (String) audio.getOrDefault("mime_type", "");

        builder.mediaId(mediaId);
        builder.mimeType(mimeType);
        findUserByPhone(from).ifPresent(builder::owner);

        WhatsAppMessage savedMessage = messageRepository.save(builder.build());
        if (mediaId == null) {
            logger.warn("Audio message without media id");
            return;
        }

        try {
            WhatsAppMediaMetadata metadata = whatsAppService.fetchMediaMetadata(mediaId)
                    .blockOptional(DEFAULT_TIMEOUT)
                    .orElseThrow(() -> new IllegalStateException("Unable to fetch audio metadata"));

            byte[] audioBytes = whatsAppService.downloadMedia(metadata.getUrl())
                    .blockOptional(DEFAULT_TIMEOUT)
                    .orElseThrow(() -> new IllegalStateException("Unable to download audio"));

            savedMessage.setMediaUrl(metadata.getUrl());
            savedMessage.setMimeType(metadata.getMimeType());

            Map<String, String> transcript = whatsAppService.transcribeWithWhisper(audioBytes).block();
            if (!StringUtils.hasText(transcript.get("text"))) {
                logger.warn("Transcription returned empty for audio message {}", savedMessage.getId());
                whatsAppService.sendTextMessage(from,
                        "Não consegui transcrever esse áudio. Poderia enviar a instrução em texto?").subscribe();
                messageRepository.save(savedMessage);
                return;
            }

            savedMessage.setTextContent(transcript.get("text"));
            messageRepository.save(savedMessage);
            /* dispatchToAssistant(savedMessage, transcript); */
            processCommandFromText(savedMessage, from);
        } catch (Exception ex) {
            logger.error("Failed to process WhatsApp audio message", ex);
            whatsAppService.sendTextMessage(from,
                    "Desculpe, não consegui processar seu áudio agora. Pode tentar novamente em instantes?")
                    .subscribe();
        }
    }

    private String transcribeAudio(byte[] audioBytes, String mimeType) {
        if (audioBytes == null || audioBytes.length == 0) {
            return null;
        }
        Optional<AiClient> client = resolveClient(AiProvider.OLLAMA);
        if (client.isEmpty()) {
            logger.warn("No AI client available to transcribe audio");
            return null;
        }

        String encoded = Base64.getEncoder().encodeToString(audioBytes);
        String prompt = """
                Transcreva o áudio enviado pelo usuário. O conteúdo do áudio está codificado em Base64 logo abaixo.
                Responda apenas com a transcrição no mesmo idioma do áudio, sem comentários adicionais.
                Tipo do arquivo: %s
                Base64: %s
                """.formatted(StringUtils.hasText(mimeType) ? mimeType : "audio/mpeg", encoded);

        AiRequest request = AiRequest.builder()
                .type(AiRequestType.TEXT)
                .model(OLLAMA_AUDIO_MODEL)
                .prompt(prompt)
                .timeout(DEFAULT_TIMEOUT)
                .build();

        AiResponse response = client.get().execute(request);
        return response != null ? sanitizeGeminiResponse(response.content()) : null;
    }

    private void processCommandFromText(WhatsAppMessage message, String from) {
        String body = Optional.ofNullable(message.getTextContent()).map(String::trim).orElse("");
        if (!StringUtils.hasText(body)) {
            return;
        }

        Users owner = Optional.ofNullable(message.getOwner())
                .or(() -> findUserByPhone(message.getFromPhone()))
                .orElse(null);
        if (owner == null) {
            logger.debug("Ignoring nutrition command because owner could not be resolved for message {}",
                    message.getId());
            return;
        }

        Optional<NutritionCommand> commandOpt = parseNutritionCommand(body);
        if (commandOpt.isEmpty()) {
            return;
        }

        NutritionCommand command = commandOpt.get();
        if (command.action() == NutritionCommandAction.UNKNOWN) {
            logger.debug("Message {} does not contain a nutrition command", message.getId());
            return;
        }

        Optional<String> response = executeNutritionCommand(owner, message, command);
        response.ifPresent(reply -> {
            String target = StringUtils.hasText(from) ? from : message.getFromPhone();
            if (StringUtils.hasText(target)) {
                whatsAppService.sendTextMessage(target, reply).subscribe();
            }
        });
    }

    private Optional<NutritionCommand> parseNutritionCommand(String body) {
        AiModelSelection commandConfig = resolveModelSelection(commandModel, commandProvider, AiProvider.GEMINI,
                "gemini-pro");
        AiProvider provider = commandConfig.provider();
        String model = commandConfig.model();

        Optional<AiClient> client = resolveClient(provider);
        if (client.isEmpty()) {
            logger.warn("{} client not configured; skipping nutrition command extraction", provider);
            return Optional.empty();
        }

        String prompt = COMMAND_EXTRACTION_PROMPT + "\nMensagem do usuário:\n" + body;
        AiRequest request = AiRequest.builder()
                .type(AiRequestType.TEXT)
                .model(model)
                .prompt(prompt)
                .timeout(DEFAULT_TIMEOUT)
                .build();

        AiResponse response = client.get().execute(request);
        if (response == null || !StringUtils.hasText(response.content())) {
            return Optional.empty();
        }

        String sanitized = sanitizeGeminiResponse(response.content());
        try {
            NutritionCommandPayload payload = objectMapper.readValue(sanitized, NutritionCommandPayload.class);
            NutritionCommand command = toNutritionCommand(payload);
            return Optional.ofNullable(command);
        } catch (JsonProcessingException ex) {
            logger.error("Failed to parse nutrition command payload: {}", sanitized, ex);
            return Optional.empty();
        }
    }

    private NutritionCommand toNutritionCommand(NutritionCommandPayload payload) {
        if (payload == null) {
            return null;
        }
        NutritionCommandAction action = NutritionCommandAction.from(payload.action());
        String mealCode = normalizeMealCode(payload.meal());
        List<NutritionCommandItem> items = Optional.ofNullable(payload.items())
                .orElse(List.of())
                .stream()
                .map(this::toNutritionCommandItem)
                .filter(item -> item != null && StringUtils.hasText(item.foodName()))
                .collect(Collectors.toList());
        String observation = sanitizeObservation(payload.observation());
        return new NutritionCommand(action, mealCode, items, observation);
    }

    private NutritionCommandItem toNutritionCommandItem(NutritionCommandItemPayload payload) {
        if (payload == null) {
            return null;
        }
        String food = sanitizeFoodName(payload.food());
        Double quantity = payload.quantity();
        String unit = sanitizeUnit(payload.unit());
        String observation = sanitizeObservation(payload.observation());
        return new NutritionCommandItem(food, quantity, unit, observation);
    }

    private String sanitizeFoodName(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private String sanitizeObservation(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private String sanitizeUnit(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeMealCode(String meal) {
        if (!StringUtils.hasText(meal)) {
            return null;
        }
        String normalized = normalizeMealKey(meal);
        if (!StringUtils.hasText(normalized)) {
            return null;
        }
        return switch (normalized) {
            case "BREAKFAST", "CAFEDAMANHA" -> "BREAKFAST";
            case "LUNCH", "ALMOCO" -> "LUNCH";
            case "DINNER", "JANTAR" -> "DINNER";
            case "SNACK", "LANCHE" -> "SNACK";
            case "SUPPER", "CEIA" -> "SUPPER";
            default -> "OTHER_MEALS";
        };
    }

    private Optional<String> executeNutritionCommand(Users owner, WhatsAppMessage source, NutritionCommand command) {
        List<String> successes = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        try {
            switch (command.action()) {
                case ADD -> processAddCommand(owner, source, command, successes, errors);
                case DELETE -> processDeleteCommand(owner, command, successes, errors);
                case EDIT -> processEditCommand(owner, source, command, successes, errors);
                default -> {
                    return Optional.empty();
                }
            }
        } catch (Exception ex) {
            logger.error("Failed to execute nutrition command", ex);
            errors.add("Ocorreu um erro ao processar sua solicitação.");
        }

        String response = buildCommandResponse(command.action(), command.mealCode(), successes, errors);
        if (!StringUtils.hasText(response)) {
            return Optional.empty();
        }
        return Optional.of(response);
    }

    private void processAddCommand(Users owner, WhatsAppMessage source, NutritionCommand command,
            List<String> successes, List<String> errors) {
        Meal meal = resolveCommandMeal(command.mealCode());
        if (meal == null) {
            errors.add("Não consegui identificar a refeição mencionada.");
            return;
        }
        if (command.items().isEmpty()) {
            errors.add("Não encontrei alimentos para adicionar.");
            return;
        }

        for (NutritionCommandItem item : command.items()) {
            try {
                WhatsAppMessage entry = upsertCommandEntry(owner, source, meal, command, item);
                if (entry != null) {
                    successes.add(describeItem(item));
                }
            } catch (Exception ex) {
                logger.error("Failed to add nutrition entry {}", item.foodName(), ex);
                errors.add("Não consegui adicionar " + item.foodName());
            }
        }
    }

    private void processDeleteCommand(Users owner, NutritionCommand command, List<String> successes,
            List<String> errors) {
        Meal meal = resolveCommandMeal(command.mealCode());
        if (meal == null) {
            errors.add("Não consegui identificar a refeição mencionada.");
            return;
        }
        if (owner.getId() == null) {
            errors.add("Usuário não identificado para remover itens.");
            return;
        }

        List<WhatsAppMessage> entries = messageRepository
                .findByOwnerIdAndNutritionAnalysisMealCodeIgnoreCase(owner.getId(), meal.getCode());
        if (entries.isEmpty()) {
            errors.add("Não encontrei registros dessa refeição para remover.");
            return;
        }

        if (command.items().isEmpty()) {
            entries.forEach(messageRepository::delete);
            successes.add("todos os itens");
            return;
        }

        for (NutritionCommandItem item : command.items()) {
            Optional<WhatsAppMessage> match = findExistingEntry(entries, item.foodName());
            if (match.isPresent()) {
                messageRepository.delete(match.get());
                successes.add(describeItem(item));
            } else {
                errors.add("Não encontrei " + item.foodName() + " nessa refeição.");
            }
        }
    }

    private void processEditCommand(Users owner, WhatsAppMessage source, NutritionCommand command,
            List<String> successes, List<String> errors) {
        Meal meal = resolveCommandMeal(command.mealCode());
        if (meal == null) {
            errors.add("Não consegui identificar a refeição mencionada.");
            return;
        }
        if (owner.getId() == null) {
            errors.add("Usuário não identificado para atualizar itens.");
            return;
        }

        List<WhatsAppMessage> existing = messageRepository
                .findByOwnerIdAndNutritionAnalysisMealCodeIgnoreCase(owner.getId(), meal.getCode());
        existing.forEach(messageRepository::delete);

        if (command.items().isEmpty()) {
            successes.add("Removi os registros do " + formatMealTypeLabel(meal.getCode()));
            return;
        }

        for (NutritionCommandItem item : command.items()) {
            try {
                WhatsAppMessage entry = upsertCommandEntry(owner, source, meal, command, item);
                if (entry != null) {
                    successes.add(describeItem(item));
                }
            } catch (Exception ex) {
                logger.error("Failed to edit nutrition entry {}", item.foodName(), ex);
                errors.add("Não consegui atualizar " + item.foodName());
            }
        }
    }

    private String buildCommandResponse(NutritionCommandAction action, String mealCode, List<String> successes,
            List<String> errors) {
        StringBuilder builder = new StringBuilder();
        String mealLabel = formatMealTypeLabel(mealCode);

        if (!successes.isEmpty()) {
            String items = String.join(", ", successes);
            builder.append(switch (action) {
                case ADD -> "Adicionei %s em %s.".formatted(items, mealLabel);
                case DELETE -> "Removi %s de %s.".formatted(items, mealLabel);
                case EDIT -> "Atualizei %s em %s.".formatted(items, mealLabel);
                default -> "";
            });
        }

        if (!errors.isEmpty()) {
            if (builder.length() > 0) {
                builder.append(" ");
            }
            builder.append(String.join(" ", errors));
        }

        return builder.toString().trim();
    }

    private Meal resolveCommandMeal(String mealCode) {
        Meal meal = null;
        if (StringUtils.hasText(mealCode)) {
            meal = resolveMealByType(mealCode);
        }
        return meal != null ? meal : resolveOtherMeal();
    }

    private Optional<WhatsAppMessage> findExistingEntry(Users owner, Meal meal, String foodName) {
        if (owner == null || owner.getId() == null || meal == null || !StringUtils.hasText(foodName)) {
            return Optional.empty();
        }
        List<WhatsAppMessage> entries = messageRepository
                .findByOwnerIdAndNutritionAnalysisMealCodeIgnoreCase(owner.getId(), meal.getCode());
        return findExistingEntry(entries, foodName);
    }

    private Optional<WhatsAppMessage> findExistingEntry(List<WhatsAppMessage> entries, String foodName) {
        if (entries == null || entries.isEmpty() || !StringUtils.hasText(foodName)) {
            return Optional.empty();
        }
        String target = normalizeFoodKey(foodName);
        return entries.stream()
                .filter(msg -> msg.getNutritionAnalysis() != null)
                .filter(msg -> {
                    String candidate = Optional.ofNullable(msg.getNutritionAnalysis().getFoodName()).orElse("");
                    return normalizeFoodKey(candidate).equals(target);
                })
                .findFirst();
    }

    private String normalizeFoodKey(String value) {
        return StringUtils.hasText(value) ? normalizeMealKey(value) : "";
    }

    private WhatsAppMessage upsertCommandEntry(Users owner, WhatsAppMessage source, Meal meal, NutritionCommand command,
            NutritionCommandItem item) {
        Optional<WhatsAppMessage> existing = findExistingEntry(owner, meal, item.foodName());
        WhatsAppMessage target = existing.orElseGet(() -> WhatsAppMessage.builder()
                .owner(owner)
                .manualEntry(true)
                .editedEntry(false)
                .messageType("MANUAL")
                .fromPhone(source.getFromPhone())
                .toPhone(source.getToPhone())
                .receivedAt(OffsetDateTime.now())
                .build());

        target.setOwner(owner);
        target.setManualEntry(true);
        target.setEditedEntry(existing.isPresent());
        target.setMessageType(existing.map(WhatsAppMessage::getMessageType).orElse("MANUAL"));
        target.setTextContent(buildCommandText(item, meal));

        WhatsAppMessage savedMessage = messageRepository.save(target);

        NutritionAnalysis analysis = Optional.ofNullable(savedMessage.getNutritionAnalysis())
                .orElse(new NutritionAnalysis());
        analysis.setMessage(savedMessage);
        analysis.setMeal(meal);

        Food resolvedFood = resolveFoodEntity(item.foodName());
        if (resolvedFood != null) {
            analysis.setFood(resolvedFood);
            analysis.setFoodName(resolvedFood.getName());
            analysis.setPrimaryCategory(resolvedFood.getFoodCategory());
            analysis.setCalories(resolvedFood.getAverageCalories());
            analysis.setProtein(resolvedFood.getAverageProtein());
            analysis.setCarbs(resolvedFood.getAverageCarbs());
            analysis.setFat(resolvedFood.getAverageFat());
        } else {
            analysis.setFood(null);
            analysis.setFoodName(item.foodName());
            analysis.setPrimaryCategory(null);
        }

        analysis.setSummary(buildCommandSummary(item, command.observation()));
        analysis.setCaloriesUnit(resolveMeasurementUnit(Optional.ofNullable(analysis.getCaloriesUnit())
                .map(MeasurementUnits::getId).orElse(null), "KCAL"));
        analysis.setProteinUnit(resolveMeasurementUnit(Optional.ofNullable(analysis.getProteinUnit())
                .map(MeasurementUnits::getId).orElse(null), "G"));
        analysis.setCarbsUnit(resolveMeasurementUnit(Optional.ofNullable(analysis.getCarbsUnit())
                .map(MeasurementUnits::getId).orElse(null), "G"));
        analysis.setFatUnit(resolveMeasurementUnit(Optional.ofNullable(analysis.getFatUnit())
                .map(MeasurementUnits::getId).orElse(null), "G"));

        MeasurementUnits quantityUnit = resolveUnitByKeyword(item.unitKeyword());
        if (quantityUnit != null && quantityUnit.getUnitType() == MeasurementUnits.UnitType.VOLUME) {
            analysis.setLiquidUnit(quantityUnit);
            analysis.setLiquidVolume(toBigDecimal(item.quantity()));
        } else {
            analysis.setLiquidUnit(null);
            analysis.setLiquidVolume(null);
        }

        NutritionAnalysis savedAnalysis = nutritionAnalysisRepository.save(analysis);
        savedMessage.setNutritionAnalysis(savedAnalysis);
        return messageRepository.save(savedMessage);
    }

    private String buildCommandSummary(NutritionCommandItem item, String generalObservation) {
        List<String> parts = new ArrayList<>();
        if (item.quantity() != null) {
            String quantity = formatQuantity(item.quantity());
            String unit = StringUtils.hasText(item.unitKeyword()) ? item.unitKeyword() : "";
            parts.add("Quantidade: " + quantity + (StringUtils.hasText(unit) ? " " + unit : ""));
        }
        if (StringUtils.hasText(item.observation())) {
            parts.add(item.observation());
        }
        if (StringUtils.hasText(generalObservation)) {
            parts.add(generalObservation);
        }
        return parts.isEmpty() ? null : String.join(" | ", parts);
    }

    private String buildCommandText(NutritionCommandItem item, Meal meal) {
        String description = describeItem(item);
        String mealLabel = formatMealTypeLabel(meal != null ? meal.getCode() : null);
        return StringUtils.hasText(description) ? description + " - " + mealLabel : mealLabel;
    }

    private String describeItem(NutritionCommandItem item) {
        StringBuilder builder = new StringBuilder();
        if (item.quantity() != null) {
            builder.append(formatQuantity(item.quantity()));
        }
        if (StringUtils.hasText(item.unitKeyword())) {
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(item.unitKeyword());
        }
        if (builder.length() > 0) {
            builder.append(" de ");
        }
        builder.append(item.foodName());
        return builder.toString().trim();
    }

    private String formatQuantity(Double value) {
        if (value == null) {
            return "";
        }
        BigDecimal decimal = BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros();
        return decimal.scale() <= 0 ? String.valueOf(decimal.longValue()) : decimal.toPlainString();
    }

    private Food resolveFoodEntity(String foodName) {
        if (!StringUtils.hasText(foodName)) {
            return null;
        }
        String normalized = normalizeFoodKey(foodName);
        Food cached = foodByNormalizedName.get(normalized);
        if (cached != null) {
            return cached;
        }

        Food resolved = foodRepository.findFirstByNameIgnoreCase(foodName)
                .or(() -> foodRepository.findFirstByNameContainingIgnoreCase(foodName))
                .orElseGet(() -> foods().stream()
                        .filter(food -> normalizeFoodKey(food.getName()).equals(normalized))
                        .findFirst()
                        .orElse(null));

        if (resolved != null) {
            foodByNormalizedName.put(normalized, resolved);
        }
        return resolved;
    }

    private MeasurementUnits resolveUnitByKeyword(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null;
        }
        String normalized = normalizeUnitKeyword(keyword);
        if (!StringUtils.hasText(normalized)) {
            return null;
        }
        MeasurementUnits cached = measurementUnitByKeyword.get(normalized);
        if (cached != null) {
            return cached;
        }

        MeasurementUnits resolved = findMeasurementUnit(normalized);
        if (resolved != null) {
            measurementUnitByKeyword.put(normalized, resolved);
        }
        return resolved;
    }

    private MeasurementUnits findMeasurementUnit(String normalized) {
        if (!StringUtils.hasText(normalized)) {
            return null;
        }

        MeasurementUnits aliasMatch = measurementUnitAliases().get(normalized);
        if (aliasMatch != null) {
            return aliasMatch;
        }

        Optional<MeasurementUnits> direct = measurementUnitRepository
                .findByCodeIgnoreCase(normalized.toUpperCase(Locale.ROOT));
        if (direct.isPresent()) {
            MeasurementUnits unit = direct.get();
            registerMeasurementUnitAliases(unit);
            return unit;
        }

        return measurementUnits().stream()
                .filter(unit -> unitMatchesKeyword(unit, normalized))
                .findFirst()
                .map(unit -> {
                    registerMeasurementUnitAliases(unit);
                    return unit;
                })
                .orElse(null);
    }

    private boolean unitMatchesKeyword(MeasurementUnits unit, String normalizedKeyword) {
        if (unit == null || !StringUtils.hasText(normalizedKeyword)) {
            return false;
        }
        return measurementUnitKeywords(unit).stream()
                .anyMatch(candidate -> normalizedKeyword.equals(candidate)
                        || candidate.contains(normalizedKeyword)
                        || normalizedKeyword.contains(candidate));
    }

    private String normalizeUnitKeyword(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return normalized.toLowerCase(Locale.ROOT).replaceAll("[^a-z]", "");
    }

    private List<MeasurementUnits> measurementUnits() {
        List<MeasurementUnits> snapshot = cachedMeasurementUnits;
        if (snapshot == null) {
            snapshot = measurementUnitRepository.findAll();
            cachedMeasurementUnits = snapshot;
            measurementUnitAliasIndex = null;
        }
        return snapshot;
    }

    private Map<String, MeasurementUnits> measurementUnitAliases() {
        Map<String, MeasurementUnits> snapshot = measurementUnitAliasIndex;
        if (snapshot == null) {
            synchronized (this) {
                snapshot = measurementUnitAliasIndex;
                if (snapshot == null) {
                    snapshot = buildMeasurementUnitAliasIndex(measurementUnits());
                    measurementUnitAliasIndex = snapshot;
                }
            }
        }
        return snapshot;
    }

    private Map<String, MeasurementUnits> buildMeasurementUnitAliasIndex(List<MeasurementUnits> units) {
        Map<String, MeasurementUnits> aliases = new ConcurrentHashMap<>();
        if (units == null) {
            return aliases;
        }
        for (MeasurementUnits unit : units) {
            registerMeasurementUnitAliases(unit, aliases);
        }
        return aliases;
    }

    private void registerMeasurementUnitAliases(MeasurementUnits unit) {
        Map<String, MeasurementUnits> aliases = measurementUnitAliases();
        registerMeasurementUnitAliases(unit, aliases);
    }

    private void registerMeasurementUnitAliases(MeasurementUnits unit, Map<String, MeasurementUnits> aliases) {
        if (unit == null || aliases == null) {
            return;
        }
        for (String keyword : measurementUnitKeywords(unit)) {
            if (StringUtils.hasText(keyword)) {
                aliases.putIfAbsent(keyword, unit);
            }
        }
    }

    private Set<String> measurementUnitKeywords(MeasurementUnits unit) {
        if (unit == null) {
            return Set.of();
        }
        Set<String> keywords = new LinkedHashSet<>();
        addKeywordVariants(keywords, unit.getCode());
        addKeywordVariants(keywords, unit.getSymbol());
        addKeywordVariants(keywords, unit.getDescription());
        if (StringUtils.hasText(unit.getDescription())) {
            String[] segments = unit.getDescription().split("[\\s,;/()]+");
            for (String segment : segments) {
                addKeywordVariants(keywords, segment);
            }
        }
        return keywords;
    }

    private void addKeywordVariants(Set<String> keywords, String value) {
        if (!StringUtils.hasText(value)) {
            return;
        }
        String normalized = normalizeUnitKeyword(value);
        if (!StringUtils.hasText(normalized)) {
            return;
        }
        keywords.add(normalized);
        for (String plural : buildPluralForms(normalized)) {
            if (StringUtils.hasText(plural)) {
                keywords.add(plural);
            }
        }
    }

    private Set<String> buildPluralForms(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return Set.of();
        }
        Set<String> forms = new LinkedHashSet<>();
        if (!keyword.endsWith("s")) {
            forms.add(keyword + "s");
        }
        if (keyword.endsWith("m") && keyword.length() > 1) {
            forms.add(keyword.substring(0, keyword.length() - 1) + "ns");
        }
        if (keyword.endsWith("l") && keyword.length() > 1) {
            forms.add(keyword.substring(0, keyword.length() - 1) + "is");
        }
        if (keyword.endsWith("cao") && keyword.length() > 3) {
            forms.add(keyword.substring(0, keyword.length() - 3) + "coes");
        } else if (keyword.endsWith("sao") && keyword.length() > 3) {
            forms.add(keyword.substring(0, keyword.length() - 3) + "soes");
        } else if (keyword.endsWith("ao") && keyword.length() > 2) {
            forms.add(keyword.substring(0, keyword.length() - 2) + "oes");
        }
        if (keyword.endsWith("r") || keyword.endsWith("z") || keyword.endsWith("x")) {
            forms.add(keyword + "es");
        }
        if (keyword.endsWith("y") && keyword.length() > 1 && !isVowel(keyword.charAt(keyword.length() - 2))) {
            forms.add(keyword.substring(0, keyword.length() - 1) + "ies");
        }
        if (keyword.endsWith("f") && keyword.length() > 1) {
            forms.add(keyword.substring(0, keyword.length() - 1) + "ves");
        } else if (keyword.endsWith("fe") && keyword.length() > 2) {
            forms.add(keyword.substring(0, keyword.length() - 2) + "ves");
        }
        return forms;
    }

    private boolean isVowel(char value) {
        char normalized = Character.toLowerCase(value);
        return normalized == 'a' || normalized == 'e' || normalized == 'i' || normalized == 'o' || normalized == 'u';
    }

    private List<Food> foods() {
        List<Food> snapshot = cachedFoods;
        if (snapshot == null) {
            snapshot = foodRepository.findAll();
            cachedFoods = snapshot;
        }
        return snapshot;
    }

    private enum NutritionCommandAction {
        ADD,
        EDIT,
        DELETE,
        UNKNOWN;

        static NutritionCommandAction from(String value) {
            if (!StringUtils.hasText(value)) {
                return UNKNOWN;
            }
            String normalized = Normalizer.normalize(value, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
            normalized = normalized.replaceAll("[^A-Za-z]", "").toUpperCase(Locale.ROOT);
            return switch (normalized) {
                case "ADD", "ADICIONAR", "ADICIONE", "INCLUIR", "INCLUA", "INSERIR", "INSIRA", "COLOCAR",
                        "COLOQUE" ->
                    ADD;
                case "EDIT", "EDITAR", "EDITE", "ALTERAR", "ALTERE", "ATUALIZAR", "ATUALIZE", "MODIFICAR",
                        "MODIFIQUE", "AJUSTAR", "AJUSTE" ->
                    EDIT;
                case "DELETE", "REMOVER", "REMOVE", "REMOVA", "EXCLUIR", "EXCLUA", "RETIRAR", "RETIRE", "TIRAR",
                        "TIRE" ->
                    DELETE;
                default -> UNKNOWN;
            };
        }
    }

    private record NutritionCommand(NutritionCommandAction action, String mealCode, List<NutritionCommandItem> items,
            String observation) {
    }

    private record NutritionCommandItem(String foodName, Double quantity, String unitKeyword, String observation) {
    }

    private record NutritionCommandPayload(String action, String meal, List<NutritionCommandItemPayload> items,
            String observation) {
    }

    private record NutritionCommandItemPayload(String food, Double quantity, String unit, String observation) {
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
                .foodName(result.foodName())
                .calories(toBigDecimal(result.calories()))
                .protein(toBigDecimal(result.macronutrients() != null ? result.macronutrients().protein_g() : null))
                .carbs(toBigDecimal(result.macronutrients() != null ? result.macronutrients().carbs_g() : null))
                .fat(toBigDecimal(result.macronutrients() != null ? result.macronutrients().fat_g() : null))
                .summary(result.summary())
                .categoriesJson(objectMapper.writeValueAsString(result.categories()))
                .confidence(toBigDecimal(result.confidence()))
                .primaryCategory(resolvePrimaryCategory(result.categories()))
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
                .max(Comparator.comparing(c -> Optional.ofNullable(c.confidence()).orElse(0.0))).orElse(null);
        if (best == null || best.name() == null || best.name().isBlank()) {
            return null;
        }
        String normalizedName = normalizeCategoryName(best.name());
        return foodCategoryRepository
                .findByNameIgnoreCase(normalizedName)
                .orElseGet(() -> foodCategoryRepository.save(FoodCategory.builder()
                        .name(normalizedName.substring(0, 1).toUpperCase() + normalizedName.substring(1))
                        .description("Categoria identificada automaticamente").build()));
    }

    public GeminiNutritionResult requestNutritionAnalysis(byte[] imageBytes, String mimeType, Users owner) {
        AiModelSelection analysisConfig = resolveModelSelection(analysisModel, analysisProvider, AiProvider.GEMINI,
                DEFAULT_ANALYSIS_MODEL);
        return requestNutritionAnalysis(imageBytes, mimeType, owner, analysisConfig);
    }

    private GeminiNutritionResult requestNutritionAnalysis(byte[] imageBytes, String mimeType, Users owner,
            AiModelSelection analysisConfig) {
        AiProvider provider = analysisConfig != null && analysisConfig.provider() != null ? analysisConfig.provider()
                : AiProvider.GEMINI;
        String model = analysisConfig != null ? analysisConfig.model() : null;

        if (provider == AiProvider.OLLAMA) {
            return requestOllamaNutritionAnalysis(imageBytes, owner, model);
        }

        Optional<AiClient> client = resolveClient(provider);
        if (client.isEmpty()) {
            logger.warn("AI provider {} not available for nutrition analysis", provider);
            return null;
        }

        if (!StringUtils.hasText(model)) {
            model = DEFAULT_ANALYSIS_MODEL;
        }
        AiRequest request = AiRequest.builder()
                .type(AiRequestType.IMAGE)
                .model(model)
                .prompt(resolvePrompt(owner, provider, model))
                .imageBytes(imageBytes)
                .mimeType(mimeType)
                .timeout(DEFAULT_TIMEOUT)
                .build();

        AiResponse response = client.get().execute(request);
        if (response == null || response.content() == null) {
            return null;
        }

        return Optional.ofNullable(response.content())
                .map(this::sanitizeGeminiResponse)
                .map(this::deserializeNutritionResult)
                .orElse(null);
    }

    private GeminiNutritionResult requestOllamaNutritionAnalysis(byte[] imageBytes, Users owner, String configuredModel) {
        if (imageBytes == null || imageBytes.length == 0) {
            return null;
        }
        String model = resolveOllamaVisionModel(configuredModel);
        try {
            List<String> images = List.of(Base64.getEncoder().encodeToString(imageBytes));
            String prompt = resolvePrompt(owner, AiProvider.OLLAMA, model);
            OllamaResponseDTO detectionResponse = ollamaService.generate(OllamaRequestDTO.builder()
                    .model(model)
                    .prompt(prompt)
                    .stream(Boolean.FALSE)
                    .images(images)
                    .build());
            if (detectionResponse == null || !StringUtils.hasText(detectionResponse.getResponse())) {
                return null;
            }
            String detectionPayload = Optional.ofNullable(detectionResponse.getResponse())
                    .map(this::sanitizeGeminiResponse)
                    .orElse(null);
            if (!StringUtils.hasText(detectionPayload)) {
                return null;
            }

            GeminiNutritionResult detection = deserializeNutritionResult(detectionPayload);
            if (detection == null || !detection.isFood()) {
                return detection;
            }

            String summaryModel = StringUtils.hasText(configuredModel) ? configuredModel.trim()
                    : DEFAULT_ASSISTANT_MODEL;
            if (!StringUtils.hasText(summaryModel) || summaryModel.equalsIgnoreCase(model)) {
                summaryModel = DEFAULT_ASSISTANT_MODEL;
            }

            String summaryPromptTemplate = resolvePrompt(owner, AiProvider.OLLAMA, summaryModel,
                    DEFAULT_PERSONALIZED_PROMPT);
            Map<String, String> promptVariables = new HashMap<>();
            promptVariables.put("DETECTION_JSON", detectionPayload);
            List<GeminiNutritionResult.Item> detectedItems = detection.items();
            if (detectedItems != null && !detectedItems.isEmpty()) {
                String names = detectedItems.stream()
                        .filter(Objects::nonNull)
                        .map(GeminiNutritionResult.Item::name)
                        .filter(StringUtils::hasText)
                        .collect(Collectors.joining(", "));
                if (StringUtils.hasText(names)) {
                    promptVariables.put("ITEM_NAMES", names);
                }
            }
            promptVariables.putIfAbsent("ITEM_NAMES", "(no items detected)");
            if (StringUtils.hasText(detection.summary())) {
                promptVariables.put("SUMMARY", detection.summary());
            }

            String summaryPrompt = applyPromptTemplate(summaryPromptTemplate, promptVariables);
            OllamaResponseDTO summaryResponse = ollamaService.generate(OllamaRequestDTO.builder()
                    .model(summaryModel)
                    .prompt(summaryPrompt)
                    .stream(Boolean.FALSE)
                    .build());

            return Optional.ofNullable(summaryResponse)
                    .map(OllamaResponseDTO::getResponse)
                    .map(this::sanitizeGeminiResponse)
                    .map(this::deserializeNutritionResult)
                    .orElse(detection);
        } catch (Exception ex) {
            logger.error("Failed to request nutrition analysis from Ollama", ex);
            return null;
        }
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

    private AiModelSelection resolveModelSelection(String configuredModel, AiProvider configuredProvider,
            AiProvider defaultProvider, String defaultModel) {
        AiProvider provider = configuredProvider != null ? configuredProvider : defaultProvider;
        String model = StringUtils.hasText(configuredModel) ? configuredModel.trim() : null;

        if (StringUtils.hasText(model)) {
            AiModelSelection embedded = extractProviderFromSpec(model);
            if (embedded != null) {
                provider = embedded.provider() != null ? embedded.provider() : provider;
                model = StringUtils.hasText(embedded.model()) ? embedded.model() : null;
            }
        }

        if (!StringUtils.hasText(model)) {
            model = defaultModel;
        }

        if (provider == null) {
            provider = defaultProvider;
        }

        return new AiModelSelection(provider, model);
    }

    private AiModelSelection extractProviderFromSpec(String spec) {
        if (!StringUtils.hasText(spec)) {
            return null;
        }

        String trimmed = spec.trim();
        String[][] separators = new String[][] { {"::"}, {"|"}, {"=>"} };
        for (String[] separator : separators) {
            String token = separator[0];
            int idx = trimmed.indexOf(token);
            if (idx > 0) {
                String providerPart = trimmed.substring(0, idx).trim();
                String modelPart = trimmed.substring(idx + token.length()).trim();
                Optional<AiProvider> provider = parseProviderFromString(providerPart);
                if (provider.isPresent()) {
                    return new AiModelSelection(provider.get(), modelPart);
                }
            }
        }

        Optional<AiProvider> provider = parseProviderFromString(trimmed);
        if (provider.isPresent()) {
            return new AiModelSelection(provider.get(), null);
        }

        return null;
    }

    private Optional<AiProvider> parseProviderFromString(String raw) {
        if (!StringUtils.hasText(raw)) {
            return Optional.empty();
        }
        String normalized = raw.trim().toUpperCase(Locale.ROOT).replace('-', '_');
        if ("GOOGLE".equals(normalized)) {
            normalized = "GEMINI";
        }
        try {
            return Optional.of(AiProvider.valueOf(normalized));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    private String resolveOllamaVisionModel(String configuredModel) {
        if (StringUtils.hasText(configuredModel)) {
            String normalized = configuredModel.trim().toLowerCase(Locale.ROOT);
            if (normalized.contains("llava") || normalized.contains("vision")) {
                return configuredModel.trim();
            }
        }
        if (StringUtils.hasText(ollamaVisionModel)) {
            return ollamaVisionModel;
        }
        return DEFAULT_OLLAMA_VISION_MODEL;
    }

    private record AiModelSelection(AiProvider provider, String model) {
    }

    private String resolvePrompt(Users owner, AiProvider provider, String model) {
        return resolvePrompt(owner, provider, model, DEFAULT_IMAGE_PROMPT);
    }

    private String resolvePrompt(Users owner, AiProvider provider, String model, String defaultPrompt) {
        String ownerReference = owner != null ? owner.getId().toString() : null;
        return aiPromptReferenceService.resolvePrompt(PROMPT_CODE, provider, model, ownerReference)
                .map(AiPromptReferenceDTO::getPrompt)
                .filter(StringUtils::hasText)
                .map(String::trim)
                .orElse(defaultPrompt);
    }

    private String applyPromptTemplate(String prompt, Map<String, String> variables) {
        if (!StringUtils.hasText(prompt) || variables == null || variables.isEmpty()) {
            return prompt;
        }
        String result = prompt;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) {
                continue;
            }
            String placeholder = "{{" + entry.getKey() + "}}";
            result = result.replace(placeholder, entry.getValue());
        }
        return result;
    }


    private Optional<AiClient> resolveClient(AiProvider provider) {
        try {
            return Optional.of(aiClientFactory.createClient(provider));
        } catch (IllegalArgumentException ex) {
            logger.error("AI provider {} is not configured", provider, ex);
            return Optional.empty();
        }
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

    public Map<String, Object> buildNutritionCaptionVariables(GeminiNutritionResult result, Users owner,
            OffsetDateTime referenceMoment) {
        Map<String, Object> variables = new HashMap<>();
        String mealLabel = formatMealTypeLabel(result != null ? result.mealType() : null);

        String mealName = result != null && StringUtils.hasText(result.mealName()) ? result.mealName() : mealLabel;
        variables.put("meal_name", mealName);
        variables.put("portion", formatNumber(result != null ? result.portion() : null));

        String dishName = result != null && StringUtils.hasText(result.dishName()) ? result.dishName()
                : Optional.ofNullable(result != null ? result.foodName() : null).filter(StringUtils::hasText)
                        .orElse(mealName);
        variables.put("dish_name", dishName);
        String emoji = result != null && StringUtils.hasText(result.dishEmoji()) ? result.dishEmoji() : "🍽️";
        variables.put("dish_emoji", emoji);

        variables.put("protein", formatNumber(result != null && result.macronutrients() != null
                ? result.macronutrients().protein_g()
                : null));
        variables.put("carbs", formatNumber(result != null && result.macronutrients() != null
                ? result.macronutrients().carbs_g()
                : null));
        variables.put("fat", formatNumber(result != null && result.macronutrients() != null
                ? result.macronutrients().fat_g()
                : null));
        variables.put("fiber", formatNumber(result != null && result.macronutrients() != null
                ? result.macronutrients().fiber_g()
                : null));

        double calories = result != null ? optionalDouble(result.kcal() != null ? result.kcal() : result.calories()) : 0.0;
        variables.put("kcal", formatNumber(calories));

        DailyTotals totals = computeDailyTotals(owner, referenceMoment);
        variables.put("protein_total", formatNumber(totals.protein()));
        variables.put("carb_total", formatNumber(totals.carbs()));
        variables.put("fat_total", formatNumber(totals.fat()));
        double fiberTotal = totals.fiber()
                + (result != null && result.macronutrients() != null && result.macronutrients().fiber_g() != null
                        ? result.macronutrients().fiber_g()
                        : 0.0);
        variables.put("fiber_total", formatNumber(fiberTotal));
        variables.put("kcal_total", formatNumber(totals.kcal()));

        return variables;
    }

    public String buildNutritionResponse(GeminiNutritionResult result, Users owner, OffsetDateTime referenceMoment) {
        return WhatsAppCaptionTemplate.DAILY_EN.format(buildNutritionCaptionVariables(result, owner, referenceMoment));
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

    private double optionalDouble(Double value) {
        return value == null ? 0.0 : value;
    }

    private DailyTotals computeDailyTotals(Users owner, OffsetDateTime referenceMoment) {
        if (owner == null || owner.getId() == null) {
            return DailyTotals.empty();
        }
        LocalDate referenceDate = referenceMoment != null
                ? referenceMoment.atZoneSameInstant(DEFAULT_ZONE).toLocalDate()
                : LocalDate.now(DEFAULT_ZONE);
        OffsetDateTime start = startOfDay(referenceDate);
        OffsetDateTime endExclusive = endOfDayExclusive(referenceDate);
        OffsetDateTime endInclusive = endExclusive != null ? endExclusive.minusNanos(1) : null;
        List<NutritionAnalysis> analyses = endInclusive != null
                ? nutritionAnalysisRepository.findByCreatedAtBetween(start, endInclusive)
                : nutritionAnalysisRepository.findTop20ByOrderByCreatedAtDesc();
        UUID ownerId = owner.getId();
        double protein = 0;
        double carbs = 0;
        double fat = 0;
        double kcal = 0;
        for (NutritionAnalysis analysis : analyses) {
            Users analysisOwner = Optional.ofNullable(analysis.getMessage()).map(WhatsAppMessage::getOwner).orElse(null);
            if (analysisOwner != null && ownerId.equals(analysisOwner.getId())) {
                protein += optionalDouble(analysis.getProtein());
                carbs += optionalDouble(analysis.getCarbs());
                fat += optionalDouble(analysis.getFat());
                kcal += optionalDouble(analysis.getCalories());
            }
        }
        return new DailyTotals(protein, carbs, fat, 0.0, kcal);
    }

    private String formatNumber(Double value) {
        if (value == null) {
            return "";
        }
        double sanitized = optionalDouble(value);
        if (Math.abs(sanitized - Math.rint(sanitized)) < 0.005) {
            return String.format(Locale.US, "%.0f", sanitized);
        }
        return String.format(Locale.US, "%.1f", sanitized);
    }

    private String formatNumber(double value) {
        return formatNumber(Double.valueOf(value));
    }

    private record DailyTotals(double protein, double carbs, double fat, double fiber, double kcal) {
        private static DailyTotals empty() {
            return new DailyTotals(0.0, 0.0, 0.0, 0.0, 0.0);
        }
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

    public record GeminiNutritionResult(@JsonProperty("isFood") boolean isFood,
            @JsonProperty("foodName") String foodName,
            @JsonProperty("calories") Double calories,
            @JsonProperty("kcal") Double kcal,
            @JsonProperty("mealType") String mealType,
            @JsonProperty("meal_name") String mealName,
            @JsonProperty("dish_name") String dishName,
            @JsonProperty("dish_emoji") String dishEmoji,
            @JsonProperty("portion") Double portion,
            Macronutrients macronutrients,
            List<Category> categories,
            List<Item> items,
            String summary,
            Double confidence) {

        private record Macronutrients(@JsonProperty("protein_g") Double protein_g,
                @JsonProperty("carbs_g") Double carbs_g,
                @JsonProperty("fat_g") Double fat_g,
                @JsonProperty("fiber_g") Double fiber_g) {
        }

        private record Category(String name, Double confidence) {
        }

        private record Item(String name, Double confidence, @JsonProperty("portion_g") Double portion_g) {
        }
    }
}
