package com.jm.services.ai;

import com.jm.enums.AiProvider;
import com.jm.services.GeminiService;
import java.time.Duration;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class GeminiAiClient implements AiClient {

    private final GeminiService geminiService;

    @Override
    public AiProvider provider() {
        return AiProvider.GEMINI;
    }

    @Override
    public AiResponse execute(AiRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("AI request must not be null");
        }

        Mono<String> result;
        if (request.type() == AiRequestType.IMAGE) {
            if (request.imageBytes() == null || request.mimeType() == null) {
                return AiResponse.withContent(null);
            }
            result = geminiService.generateTextFromImage(request.prompt(), request.imageBytes(), request.mimeType());
        } else {
            result = geminiService.generateTextFromPrompt(request.prompt());
        }

        Duration timeout = Optional.ofNullable(request.timeout()).orElse(Duration.ofSeconds(30));
        String content = result.blockOptional(timeout).orElse(null);
        return AiResponse.withContent(content);
    }
}
