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
    NutritionSummary nutrition;

    @Value
    @Builder
    public static class NutritionSummary {
        String foodName;
        double calories;
        double protein;
        double carbs;
        double fat;
        String primaryCategory;
        String summary;
    }
}
