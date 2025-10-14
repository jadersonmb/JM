package com.jm.dto;

import com.jm.enums.BodyPart;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PhotoEvolutionUpdateRequest {

    private BodyPart bodyPart;
    private LocalDate capturedAt;
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
