package com.jm.listeners;

import com.jm.events.NutritionAnalysisRequestEvent;
import com.jm.services.WhatsAppNutritionService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NutritionAnalysisEventListener {

    private static final Logger logger = LoggerFactory.getLogger(NutritionAnalysisEventListener.class);

    private final WhatsAppNutritionService whatsAppNutritionService;

    @Async
    @EventListener
    public void handleNutritionRequest(NutritionAnalysisRequestEvent event) {
        try {
            logger.info("Processing {} nutrition request for message {}...", event.getProvider(),
                    event.getMessage().getId());
            whatsAppNutritionService.processNutritionAnalysis(event.getMessage(), event.getImageBytes(),
                    event.getMimeType(), event.getOwner(), event.getFrom(), event.getProvider(), event.getModel());
        } catch (Exception ex) {
            logger.error("Failed to process {} nutrition request for message {}", event.getProvider(),
                    event.getMessage().getId(), ex);
        }
    }
}
