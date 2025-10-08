package com.jm.dto;

import com.jm.entity.MeasurementUnits;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeasurementUnitDTO {

    private UUID id;
    private String code;
    private String name;
    private String symbol;
    private MeasurementUnits.UnitType unitType;
    private Double conversionFactor;
    private Boolean baseUnit;
    private String description;
}
