// com.jm.services.OllamaService
package com.jm.services;

import com.jm.dto.OllamaRequestDTO;
import com.jm.dto.OllamaResponseDTO;
import com.jm.execption.JMException;
import com.jm.execption.ProblemType;
import com.jm.repository.OllamaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.List;

@Service
public class OllamaService {
    private static final Logger logger = LoggerFactory.getLogger(OllamaService.class);
    private static final List<String> ALLOWED_MIME = List.of("image/jpeg", "image/png", "image/webp");

    private final OllamaRepository repository;
    private final MessageSource messageSource;

    public OllamaService(OllamaRepository repository, MessageSource messageSource) {
        this.repository = repository;
        this.messageSource = messageSource;
    }

    public OllamaResponseDTO generate(OllamaRequestDTO request) {
        try {
            logger.info("Sending prompt to Ollama model: {}", request.getModel());
            return repository.sendPrompt(request);
        } catch (Exception e) {
            throw externalError(e, "Ollama");
        }
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
