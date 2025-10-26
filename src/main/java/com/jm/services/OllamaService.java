// com.jm.services.OllamaService
package com.jm.services;

import com.flickr4java.flickr.people.User;
import com.jm.dto.OllamaDTO;
import com.jm.dto.OllamaRequestDTO;
import com.jm.dto.OllamaResponseDTO;
import com.jm.entity.Ollama;
import com.jm.enums.OllamaStatus;
import com.jm.events.OllamaRequestEvent;
import com.jm.execption.JMException;
import com.jm.execption.ProblemType;
import com.jm.mappers.OllamaMapper;
import com.jm.repository.OllamaRepository;
import com.stripe.model.tax.Registration.CountryOptions.Us;

import lombok.AllArgsConstructor;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

@Service
public class OllamaService {

    private static final Logger logger = LoggerFactory.getLogger(OllamaService.class);
    private static final List<String> ALLOWED_MIME = List.of("image/jpeg", "image/png", "image/webp");

    private final OllamaRepository repository;
    private final MessageSource messageSource;
    private final WebClient webClient;
    @Value("${ollama.api.url}")
    private String baseUrl;
    private final ApplicationEventPublisher eventPublisher;
    private final OllamaMapper mapper;
    private final UserService userService;

    public OllamaService(OllamaRepository repository, MessageSource messageSource, WebClient.Builder webClientBuilder,
            ApplicationEventPublisher eventPublisher, OllamaMapper mapper, UserService userService) {
        this.userService = userService;
        this.mapper = mapper;
        this.repository = repository;
        this.messageSource = messageSource;
        this.webClient = webClientBuilder.build();
        this.eventPublisher = eventPublisher;
    }

    public OllamaResponseDTO generate(OllamaRequestDTO request) {
        try {
            logger.info("Sending prompt to Ollama model: {}", request.getModel());
            return sendPrompt(request);
        } catch (Exception e) {
            throw externalError(e, "Ollama");
        }
    }

    public OllamaResponseDTO sendPrompt(OllamaRequestDTO request) {
        return webClient.post()
                .uri(baseUrl + "/api/generate")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(OllamaResponseDTO.class)
                .block();
    }

    public OllamaDTO createAndDispatchRequest(OllamaRequestDTO ollama) {
        Ollama entity = new Ollama();
        entity.setUser(userService.findEntityById(ollama.getUserId()));
        entity.setFrom(ollama.getFrom());
        entity.setModel(ollama.getModel());
        entity.setPrompt(ollama.getPrompt());
        entity.setStatus(OllamaStatus.PENDING);
        entity.setStartedAt(LocalDateTime.now());
        entity.setImages(ollama.getImages() != null ? ollama.getImages().getFirst() : StringUtils.EMPTY);
        entity.setMetadata(ollama.getMetadata());
        Ollama saved = repository.save(entity);

        /* dispatch event async */
        eventPublisher.publishEvent(new OllamaRequestEvent(this, saved));

        logger.info("Ollama request {} dispatched for model {}", saved.getId(), saved.getModel());
        return mapper.toDTO(entity);
    }

    public List<String> encodeImages(List<MultipartFile> files) {
        try {
            if (files == null || files.isEmpty())
                return List.of();
            for (MultipartFile f : files) {
                if (!ALLOWED_MIME.contains(f.getContentType())) {
                    String msg = msg("ollama.image.invalid-type");
                    throw new JMException(HttpStatus.BAD_REQUEST.value(),
                            ProblemType.INVALID_DATA.getTitle(), ProblemType.INVALID_DATA.getUri(), msg);
                }
            }
            return files.stream().map(f -> {
                try {
                    return Base64.getEncoder().encodeToString(f.getBytes());
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }).toList();
        } catch (JMException e) {
            throw e;
        } catch (Exception e) {
            String msg = msg("ollama.image.encode-error");
            throw new JMException(HttpStatus.BAD_REQUEST.value(),
                    ProblemType.INVALID_DATA.getTitle(), ProblemType.INVALID_DATA.getUri(), msg);
        }
    }

    private JMException externalError(Exception e, String serviceName) {
        String message = msgArgs(ProblemType.EXTERNAL_SERVICE_ERROR.getMessageSource(), serviceName);
        return new JMException(HttpStatus.BAD_GATEWAY.value(),
                ProblemType.EXTERNAL_SERVICE_ERROR.getTitle(),
                ProblemType.EXTERNAL_SERVICE_ERROR.getUri(),
                message);
    }

    private String msg(String code) {
        return messageSource.getMessage(code, null, LocaleContextHolder.getLocale());
    }

    private String msgArgs(String code, Object... args) {
        return messageSource.getMessage(code, args, LocaleContextHolder.getLocale());
    }
}
