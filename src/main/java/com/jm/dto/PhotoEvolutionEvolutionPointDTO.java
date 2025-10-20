package com.jm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhotoEvolutionEvolutionPointDTO {

    private LocalDate date;
    private Double measurement;
    private String imageUrl;
}
