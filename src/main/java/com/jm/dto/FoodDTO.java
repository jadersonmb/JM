package com.jm.dto;

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
public class FoodDTO {

    private UUID id;
    private String code;
    private String name;
    private String description;
    private UUID categoryId;
    private String categoryName;
    private BigDecimal averageCalories;
    private BigDecimal averageProtein;
    private BigDecimal averageCarbs;
    private BigDecimal averageFat;
    private BigDecimal commonPortion;
    private UUID commonPortionUnitId;
    private String commonPortionUnitName;
}
