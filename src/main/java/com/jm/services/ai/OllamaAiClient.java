package com.jm.services.ai;

import com.jm.dto.OllamaRequestDTO;
import com.jm.dto.OllamaRequestDTO.OllamaRequestDTOBuilder;
import com.jm.dto.OllamaDTO;
import com.jm.enums.AiProvider;
import com.jm.services.OllamaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OllamaAiClient implements AiClient {

    private final OllamaService ollamaService;

    @Override
    public AiProvider provider() {
        return AiProvider.OLLAMA;
    }

    @Override
    public AiResponse execute(AiRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("AI request must not be null");
        }

        OllamaRequestDTOBuilder builder = OllamaRequestDTO.builder()
                .model(request.model())
                .prompt(request.prompt())
                .stream(request.streamEnabled())
                .from(request.from())
                .metadata(request.metadata());

        if (request.userId() != null) {
            builder.userId(request.userId());
        }

        if (request.attachments() != null && !request.attachments().isEmpty()) {
            builder.images(ollamaService.encodeImages(request.attachments()));
        }

        OllamaDTO response = ollamaService.createAndDispatchRequest(builder.build());
        return AiResponse.withRequestId(response.getId());
    }
}
