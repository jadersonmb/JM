package com.jm.dto.food;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoodSuggestionDTO {

    private UUID id;
    private String name;
    private String categoryName;
    private Double calories;
    private Double protein;
    private Double carbs;
    private Double fat;
    private Double fiber;
    private Double portion;
    private String portionUnit;
    private String portionLabel;
    private String primary;
}
