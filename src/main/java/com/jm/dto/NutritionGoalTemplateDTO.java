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
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "nutrition_goal_template")
public class NutritionGoalTemplateDTO {

    private UUID id;
    private String name;
    private String description;
    private NutritionGoalType type;
    private BigDecimal targetValue;
    private UUID unitId;
    private String unitName;
    private String unitSymbol;
    private NutritionGoalPeriodicity periodicity;
    private Integer customPeriodDays;
    private NutritionGoalTargetMode targetMode;
    private String notes;
    private Boolean active;
}
