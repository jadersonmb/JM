package com.jm.dto;

import com.jm.enums.ExerciseIntensity;
import lombok.Data;

import java.util.UUID;

@Data
public class ExerciseFilter {

    private String search;
    private UUID referenceId;
    private ExerciseIntensity intensity;
    private UUID userId;
    private Integer minDuration;
    private Integer maxDuration;
    private Integer minCalories;
    private Integer maxCalories;
}
