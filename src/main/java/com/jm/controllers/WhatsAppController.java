package com.jm.controllers;

import com.jm.dto.NutritionDashboardDTO;
import com.jm.dto.WhatsAppMessageDTO;
import com.jm.dto.WhatsAppMessageFeedDTO;
import com.jm.dto.WhatsAppMessageResponse;
import com.jm.services.WhatsAppNutritionService;
import com.jm.services.WhatsAppService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/public/api/v1/whatsapp")
@RequiredArgsConstructor
@Tag(name = "WhatsApp Webhook", description = "Public WhatsApp integration callbacks")
public class WhatsAppController {

    private static final Logger logger = LoggerFactory.getLogger(WhatsAppController.class);

    private final WhatsAppService whatsAppService;
    private final WhatsAppNutritionService whatsappNutritionService;

    @PostMapping("/send")
    public Mono<ResponseEntity<WhatsAppMessageResponse>> sendMessage(@RequestBody WhatsAppMessageDTO dto) {
        return whatsAppService.sendMessage(dto).map(ResponseEntity::ok);
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Service is running!");
    }

    @PostMapping("/transcribe/{mediaId}")
    public Mono<ResponseEntity<Map<String, String>>> transcribe(@PathVariable String mediaId) {
        return whatsAppService.transcribeFromWhatsApp(mediaId)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> receiveMessage(@RequestBody Map<String, Object> payload) {
        logger.info("WhatsApp webhook received");
        whatsappNutritionService.handleWebhook(payload);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/messages")
    public ResponseEntity<List<WhatsAppMessageFeedDTO>> listMessages(WhatsAppMessageDTO filter,
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(whatsappNutritionService.getRecentMessagesWithFilter(filter, userId, date));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<NutritionDashboardDTO> getDashboard(
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(whatsappNutritionService.getDashboard(userId, date));
    }

    @GetMapping(value = "/messages/{id}/image", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> getMessageImage(@PathVariable UUID id) {
        Optional<WhatsAppNutritionService.ImagePayload> imagePayload = whatsappNutritionService.loadImage(id);
        if (imagePayload.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        WhatsAppNutritionService.ImagePayload payload = imagePayload.get();
        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        String mimeType = payload.mimeType();
        if (mimeType != null && !mimeType.isBlank()) {
            try {
                mediaType = MediaType.parseMediaType(mimeType);
            } catch (InvalidMediaTypeException ex) {
                logger.warn("Unsupported media type {} for message {}", mimeType, id);
            }
        }
        return ResponseEntity.ok().contentType(mediaType).body(payload.data());
    }
}
