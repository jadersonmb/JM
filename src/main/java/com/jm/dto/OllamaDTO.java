package com.jm.dto;

import com.jm.enums.OllamaStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class OllamaDTO {
    private UUID id;
    private String model;
    private String prompt;
    private String response;
    private OllamaStatus status;
    private String errorMessage;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private Long elapsedMs;
    private UUID requestedBy;
    private String images;
    private String metadata;
}
