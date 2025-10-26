package com.jm.listeners;

import com.jm.enums.OllamaStatus;
import com.jm.dto.OllamaRequestDTO;
import com.jm.dto.OllamaResponseDTO;
import com.jm.entity.Ollama;
import com.jm.events.OllamaRequestEvent;
import com.jm.repository.OllamaRepository;
import com.jm.enums.DietPlanAiSuggestionStatus;
import com.jm.services.DietPlanAiService;
import com.jm.services.DietPlanAiService.DietPlanAiCompletionResult;
import com.jm.services.OllamaService;
import com.jm.services.WhatsAppNutritionService;
import com.jm.services.WhatsAppNutritionService.GeminiNutritionResult;
import com.jm.services.WhatsAppService;
import com.jm.services.WhatsAppCaptionTemplate;

import lombok.RequiredArgsConstructor;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OllamaEventListener {

    private static final Logger logger = LoggerFactory.getLogger(OllamaEventListener.class);

    private final OllamaRepository ollamaRepository;
    private final OllamaService ollamaService;
    private final WhatsAppService whatsAppService;
    private final WhatsAppNutritionService whatsAppNutritionService;
    private final DietPlanAiService dietPlanAiService;

    @Async
    @EventListener
    public void handleOllamaRequest(OllamaRequestEvent event) {
        Ollama entity = event.getOllama();
        logger.info("Processing Ollama request {}...", entity.getId());

        entity.setStatus(OllamaStatus.PROCESSING);
        ollamaRepository.save(entity);

        long start = System.currentTimeMillis();
        try {
            boolean handledByDietService = DietPlanAiService.REQUEST_SOURCE.equalsIgnoreCase(entity.getFrom());
            if (!handledByDietService) {
                whatsAppService.sendTextMessage(entity.getFrom(), "Um momento, estou pensando ⌛️").subscribe();
            }

            OllamaResponseDTO response = ollamaService.sendPrompt(OllamaRequestDTO.builder()
                    .model(entity.getModel())
                    .prompt(entity.getPrompt())
                    .stream(Boolean.FALSE)
                    .images(entity.getImages() == null ? Collections.emptyList()
                            : Collections.singletonList(entity.getImages()))
                    .build());

            entity.setResponse(response.getResponse());
            entity.setFinishedAt(LocalDateTime.now());
            entity.setElapsedMs(System.currentTimeMillis() - start);
            if (handledByDietService) {
                Optional<DietPlanAiCompletionResult> result = dietPlanAiService.handleOllamaCompletion(entity);
                if (result.isPresent()) {
                    DietPlanAiCompletionResult completion = result.get();
                    if (completion.status() == DietPlanAiSuggestionStatus.PROCESSING) {
                        logger.warn("Diet AI completion for request {} returned PROCESSING status", entity.getId());
                    } else {
                        if (StringUtils.isNotBlank(completion.metadataJson())) {
                            entity.setMetadata(completion.metadataJson());
                        }
                        if (completion.status() == DietPlanAiSuggestionStatus.FAILED) {
                            entity.setStatus(OllamaStatus.ERROR);
                            entity.setErrorMessage(completion.errorMessage());
                        } else {
                            entity.setStatus(OllamaStatus.DONE);
                            entity.setErrorMessage(null);
                        }
                        ollamaRepository.save(entity);
                        return;
                    }
                }
            }

            entity.setStatus(OllamaStatus.DONE);
            ollamaRepository.save(entity);

            if (response.isDone()) {
                if (StringUtils.isNotBlank(entity.getImages()) && Objects.nonNull(entity.getImages())) {
                    logger.info("Ollama request image {} completed successfully", entity.getId());
                    GeminiNutritionResult deserializeNutritionResult = whatsAppNutritionService
                            .deserializeNutritionResult(sanitizeResponse(response.getResponse()));
                    Map<String, Object> captionVariables = whatsAppNutritionService
                            .buildNutritionCaptionVariables(deserializeNutritionResult, entity.getUser(),
                                    entity.getFinishedAt() != null
                                            ? entity.getFinishedAt().atOffset(java.time.ZoneOffset.UTC)
                                            : OffsetDateTime.now(ZoneOffset.UTC));
                    whatsAppService
                            .sendCaptionMessage(entity.getFrom(), WhatsAppCaptionTemplate.DAILY_EN, captionVariables)
                            .subscribe();
                    return;
                }
                logger.info("Ollama request {} completed successfully", entity.getId());
                whatsAppService.sendTextMessage(entity.getFrom(), response.getResponse()).subscribe();
                return;
            }
        } catch (Exception ex) {
            logger.error("Ollama request {} failed: {}", entity.getId(), ex.getMessage());
            entity.setStatus(OllamaStatus.ERROR);
            entity.setErrorMessage(ex.getMessage());
            entity.setFinishedAt(LocalDateTime.now());
            ollamaRepository.save(entity);
        }
    }

    private String sanitizeResponse(String raw) {
        if (raw == null) {
            return null;
        }
        String cleaned = raw.trim();
        cleaned = cleaned.replace("```json", "").replace("```", "").trim();
        return cleaned;
    }
}
