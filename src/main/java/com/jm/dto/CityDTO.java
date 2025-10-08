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
public class CityDTO {

    private UUID id;
    private UUID countryId;
    private String stateCode;
    private String stateName;
    private String cityCode;
    private String name;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Integer population;
    private String timezone;
    private Boolean capital;
}
