package com.jm.dto;

import com.jm.enums.ExerciseIntensity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExerciseDTO {

    private UUID id;
    private UUID referenceId;
    private String referenceName;
    private UUID userId;
    private String userName;
    private String customName;
    private Integer durationMinutes;
    private ExerciseIntensity intensity;
    private Integer caloriesBurned;
    private String equipment;
    private String muscleGroup;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
