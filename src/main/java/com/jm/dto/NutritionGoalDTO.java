package com.jm.dto;

import com.jm.enums.NutritionGoalPeriodicity;
import com.jm.enums.NutritionGoalTargetMode;
import com.jm.enums.NutritionGoalType;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "nutrition_goal")
public class NutritionGoalDTO {

    private UUID id;
    private NutritionGoalType type;
    private BigDecimal targetValue;
    private UUID unitId;
    private String unitName;
    private String unitSymbol;
    private NutritionGoalPeriodicity periodicity;
    private Integer customPeriodDays;
    private NutritionGoalTargetMode targetMode;
    private LocalDate startDate;
    private LocalDate endDate;
    private UUID createdByUserId;
    private String createdByUserName;
    private Boolean active;
    private String notes;
    private UUID templateId;
    private String templateName;
}
