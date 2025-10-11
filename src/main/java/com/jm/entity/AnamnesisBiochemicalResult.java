package com.jm.entity;

import jakarta.persistence.Column;
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
@Table(name = "anamnesis_biochemical_results")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnamnesisBiochemicalResult {

    @Id
    @Builder.Default
    private UUID id = UUID.randomUUID();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "anamnesis_id", nullable = false)
    private Anamnesis anamnesis;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "biochemical_exam_id", nullable = false)
    private BiochemicalExam biochemicalExam;

    @Column(name = "result_value")
    private String resultValue;

    @Column(name = "result_date")
    private LocalDate resultDate;
}
