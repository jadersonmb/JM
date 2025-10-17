package com.jm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

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
}
