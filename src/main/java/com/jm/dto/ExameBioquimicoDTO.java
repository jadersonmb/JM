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
public class ExameBioquimicoDTO {

    private UUID id;
    private String nomeExame;
    private String valor;
    private LocalDate dataExame;
}
