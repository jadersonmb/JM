package com.jm.dto;

import com.jm.enums.BiologicalSex;
import com.jm.enums.NutritionGoalObjective;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NutritionGoalCreateRequestDTO {

    private UUID userId;
    private Integer age;
    private BiologicalSex sex;
    private Double weight;
    private String weightUnit;
    private Double height;
    private String heightUnit;
    private Double heightInches;
    private NutritionGoalObjective objective;
    private Double activityFactor;
}
