package com.jm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Refeicao24hDTO {

    private UUID id;
    private String nomeRefeicao;
    private String alimentos;
    private String quantidades;
}
