package com.jm.dto;

import lombok.Builder;
import lombok.Value;

import java.time.OffsetDateTime;
import java.util.UUID;

@Value
@Builder
public class WhatsAppMessageFeedDTO {
    UUID id;
    String whatsappMessageId;
    String fromPhone;
    String toPhone;
    String messageType;
    String textContent;
    String imageUrl;
    String cloudFlareImageUrl;
    OffsetDateTime receivedAt;
    boolean manualEntry;
    UUID ownerUserId;
    MealSummary meal;
    FoodSummary food;
    LiquidSummary liquids;
    NutritionSummary nutrition;

    @Value
    @Builder
    public static class NutritionSummary {
        UUID foodId;
        String foodName;
        double calories;
        double protein;
        double carbs;
        double fat;
        UUID caloriesUnitId;
        String caloriesUnitSymbol;
        UUID proteinUnitId;
        String proteinUnitSymbol;
        UUID carbsUnitId;
        String carbsUnitSymbol;
        UUID fatUnitId;
        String fatUnitSymbol;
        UUID primaryCategoryId;
        String primaryCategory;
        String summary;
    }

    @Value
    @Builder
    public static class MealSummary {
        UUID id;
        String name;
        String code;
        String description;
        Integer sortOrder;
    }

    @Value
    @Builder
    public static class FoodSummary {
        UUID id;
        String name;
        String code;
        UUID categoryId;
        String categoryName;
    }

    @Value
    @Builder
    public static class LiquidSummary {
        double volume;
        UUID unitId;
        String unitSymbol;
        double volumeMl;
    }
}
