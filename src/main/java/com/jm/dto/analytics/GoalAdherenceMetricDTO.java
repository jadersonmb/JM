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
public class GoalAdherenceMetricDTO {

    private String key;
    private BigDecimal target;
    private BigDecimal achieved;
    private BigDecimal percent;
}
