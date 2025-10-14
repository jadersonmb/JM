package com.jm.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PhotoEvolutionPrefillDTO {

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
}
