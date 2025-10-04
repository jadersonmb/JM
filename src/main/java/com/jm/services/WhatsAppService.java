package com.jm.services;

import com.jm.dto.WhatsAppMediaMetadata;
import com.jm.dto.WhatsAppMessageDTO;
import com.jm.dto.WhatsAppMessageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

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

    public WhatsAppService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public Mono<WhatsAppMessageResponse> sendMessage(WhatsAppMessageDTO dto) {
        return webClient.post().uri(apiUrl, uriBuilder -> uriBuilder.build(phoneNumberId))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiToken).contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("messaging_product", "whatsapp", "to", dto.getPhoneNumber(), "type", "text", "text",
                        Map.of("body", dto.getMessage())))
                .retrieve().bodyToMono(WhatsAppMessageResponse.class)
                .doOnError(error -> logger.error("Failed to send WhatsApp message", error));
    }

    public Mono<Void> sendTextMessage(String phoneNumber, String message) {
        WhatsAppMessageDTO dto = new WhatsAppMessageDTO();
        dto.setPhoneNumber(phoneNumber);
        dto.setMessage(message);
        return sendMessage(dto).then();
    }

    public WhatsAppMessageResponse sendImageMessage(WhatsAppMessageDTO dto) {
        return webClient.post().uri(apiUrl, uriBuilder -> uriBuilder.build(phoneNumberId))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiToken).contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("messaging_product", "whatsapp", "to", dto.getPhoneNumber(), "type", "image", "image",
                        Map.of("link", dto.getImage().getFirst().getLink(), "caption",
                                dto.getImage().getFirst().getCaption())))
                .retrieve().bodyToMono(WhatsAppMessageResponse.class).block();
    }

    public Mono<WhatsAppMediaMetadata> fetchMediaMetadata(String mediaId) {
        return webClient.get().uri(graphBaseUrl + "/{mediaId}", mediaId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiToken).accept(MediaType.APPLICATION_JSON).retrieve()
                .bodyToMono(WhatsAppMediaMetadata.class);
    }

    public Mono<byte[]> downloadMedia(String mediaUrl) {
        return webClient.get().uri(mediaUrl).header(HttpHeaders.AUTHORIZATION, "Bearer " + apiToken).retrieve()
                .bodyToMono(byte[].class);
    }
}
