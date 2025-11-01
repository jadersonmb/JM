package com.jm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NutritionGoalCalculationResponseDTO {

    private Double bmr;
    private Double tdee;
    private Double recommendedCalories;
    private Double protein;
    private Double carbs;
    private Double fat;
    private String summary;
    private List<NutritionGoalDTO> goals;
}
