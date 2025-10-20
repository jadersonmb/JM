package com.jm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhotoEvolutionComparisonDTO {

    private UUID userId;
    private List<PhotoEvolutionBodyPartComparisonDTO> parts;
}
