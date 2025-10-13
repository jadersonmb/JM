package com.jm.dto;

import com.jm.enums.AiProvider;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiPromptReferenceDTO {

    private UUID id;
    private String code;
    private String name;
    private String description;
    private AiProvider provider;
    private String model;
    private String owner;
    private String prompt;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
