package com.jm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnamnesisBiochemicalResultDTO {

    private UUID id;
    private UUID biochemicalExamId;
    private String biochemicalExamName;
    private String resultValue;
    private LocalDate resultDate;
}
