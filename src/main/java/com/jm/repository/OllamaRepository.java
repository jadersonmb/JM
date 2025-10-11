package com.jm.repository;

import com.jm.dto.OllamaRequestDTO;
import com.jm.dto.OllamaResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.web.reactive.function.client.WebClient;

@Repository
public class OllamaRepository {

    private final WebClient webClient;

    public OllamaRepository(@Value("${ollama.api.url}") String baseUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public OllamaResponseDTO sendPrompt(OllamaRequestDTO request) {
        return webClient.post()
                .uri("/api/generate")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(OllamaResponseDTO.class)
                .block();
    }
}
