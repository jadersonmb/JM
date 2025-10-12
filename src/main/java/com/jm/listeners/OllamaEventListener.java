package com.jm.listeners;

import com.jm.enums.OllamaStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue.Builder;
import com.jm.dto.OllamaRequestDTO;
import com.jm.dto.OllamaResponseDTO;
import com.jm.entity.Ollama;
import com.jm.events.OllamaRequestEvent;
import com.jm.repository.OllamaRepository;
import com.jm.services.OllamaService;
import com.jm.services.WhatsAppNutritionService;
import com.jm.services.WhatsAppNutritionService.GeminiNutritionResult;
import com.jm.services.WhatsAppService;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class OllamaEventListener {

    private static final Logger logger = LoggerFactory.getLogger(OllamaEventListener.class);

    private final OllamaRepository ollamaRepository;
    private final OllamaService ollamaService;
    private final WhatsAppService whatsAppService;
    private final WhatsAppNutritionService whatsAppNutritionService;

    @Async
    @EventListener
    public void handleOllamaRequest(OllamaRequestEvent event) {
        Ollama entity = event.getOllama();
        logger.info("Processing Ollama request {}...", entity.getId());

        entity.setStatus(OllamaStatus.PROCESSING);
        ollamaRepository.save(entity);

        long start = System.currentTimeMillis();
        try {

            whatsAppService.sendTextMessage(entity.getFrom(), "Um momento, estou pensando ⌛️").subscribe();

            OllamaResponseDTO response = ollamaService.sendPrompt(OllamaRequestDTO.builder()
                    .model(entity.getModel())
                    .prompt(entity.getPrompt())
                    .stream(Boolean.FALSE)
                    .images(entity.getImages() == null ? Collections.emptyList()
                            : Collections.singletonList(entity.getImages()))
                    .build());

            entity.setResponse(response.getResponse());
            entity.setStatus(OllamaStatus.DONE);
            entity.setFinishedAt(LocalDateTime.now());
            entity.setElapsedMs(System.currentTimeMillis() - start);
            ollamaRepository.save(entity);

            if (response.isDone()) {
                if (Objects.nonNull(entity.getImages())) {
                    logger.info("Ollama request image {} completed successfully", entity.getId());
                    GeminiNutritionResult deserializeNutritionResult = whatsAppNutritionService
                            .deserializeNutritionResult(sanitizeResponse(response.getResponse()));
                    String nutritionResponse = whatsAppNutritionService
                            .buildNutritionResponse(deserializeNutritionResult);
                    whatsAppService.sendTextMessage(entity.getFrom(), nutritionResponse).subscribe();
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
