package com.jm.dto;

import lombok.Builder;
import lombok.Value;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Value
@Builder
public class NutritionDashboardDTO {
    double totalCalories;
    double totalProtein;
    double totalCarbs;
    double totalFat;
    double totalLiquidVolume;
    double totalLiquidVolumeMl;
    String liquidUnitSymbol;
    int mealsAnalyzed;
    Map<String, Double> categoryCalories;
    List<NutritionHistoryItem> history;

    @Value
    @Builder
    public static class NutritionHistoryItem {
        UUID messageId;
        UUID foodId;
        String foodName;
        double calories;
        double protein;
        double carbs;
        double fat;
        double liquidVolume;
        double liquidVolumeMl;
        String liquidUnitSymbol;
        UUID mealId;
        String mealName;
        String primaryCategory;
        OffsetDateTime analyzedAt;
        String summary;
    }
}
