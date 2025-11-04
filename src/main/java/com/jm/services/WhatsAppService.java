package com.jm.services;

import com.jm.dto.WhatsAppMediaMetadata;
import com.jm.dto.WhatsAppMessageDTO;
import com.jm.dto.WhatsAppMessageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class WhatsAppService {

    private static final Logger logger = LoggerFactory.getLogger(WhatsAppService.class);

    private final WebClient webClient;

    @Value("${whatsapp.api.url}")
    private String apiUrl;
    @Value("${whatsapp.api.base-url}")
    private String graphBaseUrl;
    @Value("${whatsapp.api.token}")
    private String apiToken;
    @Value("${whatsapp.phone.number.id}")
    private String phoneNumberId;
    @Value("${whatsapp.hug.token}")
    private String apiTokenHug;

    public WhatsAppService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public Mono<WhatsAppMessageResponse> sendMessage(WhatsAppMessageDTO dto) {
        return webClient.post()
                .uri(apiUrl, uriBuilder -> uriBuilder.build(phoneNumberId))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("messaging_product", "whatsapp", "to", dto.getPhoneNumber(), "type", "text", "text",
                        Map.of("body", dto.getMessage())))
                .retrieve()
                .bodyToMono(WhatsAppMessageResponse.class)
                .doOnError(error -> logger.error("Failed to send WhatsApp message", error));
    }

    public Mono<Void> sendTextMessage(String phoneNumber, String message) {
        WhatsAppMessageDTO dto = new WhatsAppMessageDTO();
        dto.setPhoneNumber(phoneNumber);
        dto.setMessage(message);
        return sendMessage(dto).then();
    }

    public Mono<Void> sendCaptionMessage(String phoneNumber, WhatsAppCaptionTemplate template, Map<String, ?> variables) {
        if (template == null) {
            return Mono.empty();
        }

        List<String> templateNames = template.templateNames();
        if (templateNames.isEmpty()) {
            logger.warn("Template {} does not have any registered WhatsApp template names", template.code());
            return Mono.empty();
        }

        return sendTemplateWithFallback(phoneNumber, template, variables, templateNames, 0);
    }

    private Mono<Void> sendTemplateWithFallback(String phoneNumber, WhatsAppCaptionTemplate template,
            Map<String, ?> variables, List<String> templateNames, int index) {
        if (index >= templateNames.size()) {
            String formatted = template.format(variables);
            if (!StringUtils.hasText(formatted)) {
                return Mono.empty();
            }
            logger.warn("All template fallbacks failed for {}. Falling back to plain text delivery.", template.code());
            return sendTextMessage(phoneNumber, formatted);
        }

        String templateName = templateNames.get(index);
        Map<String, Object> payload = template.buildTemplatePayload(phoneNumber, variables, templateName);

        return webClient.post()
                .uri(apiUrl, uriBuilder -> uriBuilder.build(phoneNumberId))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(WhatsAppMessageResponse.class)
                .doOnError(error -> {
                    if (!(error instanceof WebClientResponseException.NotFound)) {
                        logger.error("Failed to send WhatsApp template message using {}", templateName, error);
                    }
                })
                .then()
                .onErrorResume(WebClientResponseException.NotFound.class, ex -> {
                    String responseBody = ex.getResponseBodyAsString();
                    logger.warn("Template {} with name '{}' not found (response: {}). Trying next fallback if available.",
                            template.code(), templateName, StringUtils.hasText(responseBody) ? responseBody : "<empty>");
                    return sendTemplateWithFallback(phoneNumber, template, variables, templateNames, index + 1);
                });
    }

    public WhatsAppMessageResponse sendImageMessage(WhatsAppMessageDTO dto) {
        return webClient.post()
                .uri(apiUrl, uriBuilder -> uriBuilder.build(phoneNumberId))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("messaging_product", "whatsapp", "to", dto.getPhoneNumber(), "type", "image", "image",
                        Map.of("link", dto.getImage()
                                .getFirst()
                                .getLink(), "caption",
                                dto.getImage().getFirst().getCaption())))
                .retrieve()
                .bodyToMono(WhatsAppMessageResponse.class)
                .block();
    }

    /**
     * Recebe o mediaId do WhatsApp → baixa o áudio → envia ao Hugging Face →
     * retorna o texto transcrito
     */
    public Mono<Map<String, String>> transcribeFromWhatsApp(String mediaId) {
        return fetchMediaMetadata(mediaId).flatMap(meta -> {
            return downloadMedia(meta.getUrl());
        }).flatMap(audioBytes -> {
            return transcribeWithWhisper(audioBytes);
        }).onErrorResume(e -> {
            e.printStackTrace();
            return Mono.just(Collections.singletonMap("text", "❌ Erro na transcrição: " + e.getMessage()));
        });
    }

    public Mono<Map<String, String>> transcribeWithWhisper(byte[] audioBytes) {
        return webClient.post()
                .uri("https://api-inference.huggingface.co/models/openai/whisper-large-v3")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiTokenHug)
                .header(HttpHeaders.CONTENT_TYPE, "audio/ogg")
                .bodyValue(new ByteArrayResource(audioBytes) {
                    @Override
                    public String getFilename() {
                        return "audio.ogg";
                    }
                })
                .retrieve()
                .bodyToMono(Map.class)
                .map(resp -> {
                    System.out.println("🧠 Resposta HF: " + resp);
                    return resp;
                });
    }

    public Mono<WhatsAppMediaMetadata> fetchMediaMetadata(String mediaId) {
        return webClient.get().uri(graphBaseUrl + "/{mediaId}", mediaId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiToken)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(WhatsAppMediaMetadata.class);
    }

    public Mono<byte[]> downloadMedia(String mediaUrl) {
        return webClient.get()
                .uri(mediaUrl)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiToken)
                .retrieve()
                .bodyToMono(byte[].class);
    }
}
