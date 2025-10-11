package com.jm.dto;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class WhatsAppNutritionEntryRequest {

    private UUID ownerUserId;
    private OffsetDateTime receivedAt;
    private String textContent;
    private String imageUrl;
    private UUID mealId;
    private UUID foodId;
    private String foodName;
    private Double calories;
    private UUID caloriesUnitId;
    private Double protein;
    private UUID proteinUnitId;
    private Double carbs;
    private UUID carbsUnitId;
    private Double fat;
    private UUID fatUnitId;
    private Double liquidVolume;
    private UUID liquidUnitId;
    private String summary;
    private UUID primaryCategoryId;
}
