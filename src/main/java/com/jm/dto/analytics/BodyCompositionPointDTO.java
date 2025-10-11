package com.jm.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BodyCompositionPointDTO {

    private String label;
    private BigDecimal weight;
    private BigDecimal bmi;
    private BigDecimal fatPercentage;
    private BigDecimal musclePercentage;
}
