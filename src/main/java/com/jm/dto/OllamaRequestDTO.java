package com.jm.dto;

import java.util.List;
import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OllamaRequestDTO {
    private UUID userId;
    private String model;
    private String from;
    private String prompt;
    private Boolean stream;
    private List<String> images;
    private String metadata;
}
