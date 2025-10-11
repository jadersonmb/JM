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
public class AnamnesisFoodRecallItemDTO {

    private UUID id;
    private UUID foodId;
    private String foodName;
    private UUID measurementUnitId;
    private String measurementUnitSymbol;
    private BigDecimal quantity;
}
