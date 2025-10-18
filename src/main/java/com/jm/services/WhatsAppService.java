package com.jm.services;

import com.jm.dto.UserDTO;
import com.jm.dto.WhatsAppMediaMetadata;
import com.jm.dto.WhatsAppMessageDTO;
import com.jm.dto.WhatsAppMessageResponse;
import com.jm.execption.JMException;
import com.jm.execption.ProblemType;
import com.jm.services.UserService;
import com.jm.services.payment.RecurringPaymentService;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class WhatsAppService {

    private static final Logger logger = LoggerFactory.getLogger(WhatsAppService.class);

    private final WebClient webClient;
    private final UserService userService;
    private final RecurringPaymentService recurringPaymentService;
    private final MessageSource messageSource;

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

    public WhatsAppService(WebClient.Builder webClientBuilder,
            UserService userService,
            RecurringPaymentService recurringPaymentService,
            MessageSource messageSource) {
        this.webClient = webClientBuilder.build();
        this.userService = userService;
        this.recurringPaymentService = recurringPaymentService;
        this.messageSource = messageSource;
    }

    public void registerAndWelcomeUser(UUID userId) {
        UserDTO user = userService.findById(userId);
        Locale locale = resolveLocale(user.getLocale());

        if (!recurringPaymentService.hasActiveRecurringPayment(userId)) {
            throw new JMException(HttpStatus.PAYMENT_REQUIRED.value(),
                    ProblemType.PAYMENT_REQUIRED.getUri(),
                    ProblemType.PAYMENT_REQUIRED.getTitle(),
                    getMessage(ProblemType.PAYMENT_REQUIRED.getMessageSource(), locale));
        }

        String normalizedPhone = normalizePhoneNumber(user.getPhoneNumber());
        if (!StringUtils.hasText(normalizedPhone)) {
            throw new JMException(HttpStatus.BAD_REQUEST.value(),
                    ProblemType.WHATSAPP_PHONE_REQUIRED.getUri(),
                    ProblemType.WHATSAPP_PHONE_REQUIRED.getTitle(),
                    getMessage(ProblemType.WHATSAPP_PHONE_REQUIRED.getMessageSource(), locale));
        }

        registerContact(normalizedPhone, locale);
        sendWelcomeMessage(user.getName(), normalizedPhone, locale);
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

    private void registerContact(String phoneNumber, Locale locale) {
        try {
            webClient.post()
                    .uri(graphBaseUrl + "/{phoneNumberId}/contacts", phoneNumberId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of("blocking", "wait", "contacts", List.of(phoneNumber)))
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            logger.info("WhatsApp contact registered: {}", phoneNumber);
        } catch (WebClientResponseException ex) {
            logger.error("Error registering phone on Meta API: {}", ex.getResponseBodyAsString(), ex);
            throw new JMException(HttpStatus.BAD_REQUEST.value(),
                    ProblemType.WHATSAPP_API_ERROR.getUri(),
                    ProblemType.WHATSAPP_API_ERROR.getTitle(),
                    getMessage(ProblemType.WHATSAPP_API_ERROR.getMessageSource(), locale));
        } catch (Exception ex) {
            logger.error("Error registering phone on Meta API", ex);
            throw new JMException(HttpStatus.BAD_REQUEST.value(),
                    ProblemType.WHATSAPP_API_ERROR.getUri(),
                    ProblemType.WHATSAPP_API_ERROR.getTitle(),
                    getMessage(ProblemType.WHATSAPP_API_ERROR.getMessageSource(), locale));
        }
    }

    private void sendWelcomeMessage(String name, String phoneNumber, Locale locale) {
        String messageKey = locale.getLanguage().startsWith("en")
                ? "whatsapp.onboarding.welcome.en"
                : "whatsapp.onboarding.welcome.pt";
        String message = getMessage(messageKey, locale, name);
        try {
            sendTextMessage(phoneNumber, message).block();
            logger.info("Welcome message sent to {}", phoneNumber);
        } catch (Exception ex) {
            logger.error("Error sending WhatsApp welcome message: {}", ex.getMessage(), ex);
        }
    }

    private String normalizePhoneNumber(String raw) {
        if (!StringUtils.hasText(raw)) {
            return null;
        }
        String trimmed = raw.trim();
        if (trimmed.startsWith("00")) {
            trimmed = "+" + trimmed.substring(2);
        }
        if (trimmed.startsWith("+")) {
            String digits = trimmed.substring(1).replaceAll("\\D", "");
            return digits.isEmpty() ? null : "+" + digits;
        }
        String digits = trimmed.replaceAll("\\D", "");
        return digits.isEmpty() ? null : digits;
    }

    private Locale resolveLocale(String localeTag) {
        Locale fallback = LocaleContextHolder.getLocale();
        if (!StringUtils.hasText(localeTag)) {
            return fallback;
        }
        Locale locale = Locale.forLanguageTag(localeTag);
        if (!StringUtils.hasText(locale.getLanguage())) {
            return fallback;
        }
        return locale;
    }

    private String getMessage(String code, Locale locale, Object... args) {
        return messageSource.getMessage(code, args, code, locale);
    }
}
