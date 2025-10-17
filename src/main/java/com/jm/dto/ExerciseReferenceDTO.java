package com.jm.dto;

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
public class ExerciseReferenceDTO {

    private UUID id;
    private String code;
    private String name;
    private String description;
    private String muscleGroup;
    private String equipment;
    private String language;
    private LocalDateTime createdAt;
}
