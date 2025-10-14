package com.jm.dto;

import com.jm.enums.BodyPart;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhotoEvolutionDTO {

    private UUID id;
    private UUID userId;
    private String userDisplayName;
    private UUID imageId;
    private String imageUrl;
    private String imageFileName;
    private BodyPart bodyPart;
    private LocalDate capturedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private BigDecimal weight;
    private BigDecimal bodyFatPercentage;
    private BigDecimal muscleMass;
    private BigDecimal visceralFat;
    private BigDecimal waistCircumference;
    private BigDecimal hipCircumference;
    private BigDecimal chestCircumference;
    private BigDecimal leftArmCircumference;
    private BigDecimal rightArmCircumference;
    private BigDecimal leftThighCircumference;
    private BigDecimal rightThighCircumference;
    private BigDecimal caloricIntake;
    private BigDecimal proteinIntake;
    private BigDecimal carbohydrateIntake;
    private BigDecimal fatIntake;
    private String notes;
}
