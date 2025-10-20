package com.jm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhotoEvolutionBodyPartComparisonDTO {

    private String bodyPartCode;
    private String bodyPart;
    private String latestImage;
    private Double initialMeasurement;
    private Double currentMeasurement;
    private Double variation;
    private List<PhotoEvolutionEvolutionPointDTO> evolution;
}
