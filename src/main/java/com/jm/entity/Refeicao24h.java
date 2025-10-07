package com.jm.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "refeicao_24h")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Refeicao24h {

    @Id
    @Builder.Default
    private UUID id = UUID.randomUUID();

    private String nomeRefeicao;
    private String alimentos;
    private String quantidades;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "anamnese_id")
    private Anamnese anamnese;
}
