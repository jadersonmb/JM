package com.jm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DietMealItemDTO {

    private UUID id;
    private UUID dietMealId;
    private UUID foodItemId;
    private String foodItemName;
    private UUID unitId;
    private String unitDisplayName;
    private BigDecimal quantity;
    private String notes;
}

