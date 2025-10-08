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
public class BiochemicalExamDTO {

    private UUID id;
    private String code;
    private String name;
    private String description;
    private UUID measurementUnitId;
    private String measurementUnitDescription;
    private BigDecimal minReferenceValue;
    private BigDecimal maxReferenceValue;
}
