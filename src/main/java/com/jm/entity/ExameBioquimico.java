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

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "exame_bioquimico")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExameBioquimico {

    @Id
    @Builder.Default
    private UUID id = UUID.randomUUID();

    private String nomeExame;
    private String valor;
    private LocalDate dataExame;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "anamnese_id")
    private Anamnese anamnese;
}
