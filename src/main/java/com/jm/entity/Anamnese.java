package com.jm.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "anamnese")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Anamnese {

    @Id
    @Builder.Default
    private UUID id = UUID.randomUUID();

    private String mucosa;
    private String membros;
    private String cirurgias;
    private String avaliacaoObservacoes;

    private Boolean colesterol;
    private Boolean hipertensao;
    private Boolean diabetes1;
    private Boolean diabetes2;
    private Boolean trigliceridemia;
    private Boolean anemia;
    private Boolean intestinal;
    private Boolean gastrica;
    private Boolean renal;
    private Boolean hepatica;
    private Boolean vesicular;

    private BigDecimal peso;
    private BigDecimal estatura;
    private BigDecimal imc;
    private BigDecimal gorduraPercent;
    private BigDecimal musculoPercent;
    private BigDecimal tmb;
    private BigDecimal circunAbdomen;
    private BigDecimal circunCintura;
    private BigDecimal circunQuadril;
    private BigDecimal circunBraco;
    private BigDecimal circunJoelho;
    private BigDecimal circunTorax;

    private String preparoAlimentos;
    private String localRefeicao;
    private String horarioTrabalho;
    private String horarioEstudo;
    private String apetite;
    private String ingestaoHidrica;
    private String atividadeFisica;
    private String frequencia;
    private String duracao;
    private Boolean fuma;
    private Boolean bebe;
    private String suplementos;
    private String sono;
    private String mastigacao;
    private String habitosObservacoes;

    private String alimentosPreferidos;
    private String alimentosNaoGosta;

    private String diagnostico;
    @Column(length = 4000)
    private String resumoDieta;

    @OneToMany(mappedBy = "anamnese", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ExameBioquimico> examesBioquimicos = new ArrayList<>();

    @OneToMany(mappedBy = "anamnese", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Refeicao24h> refeicoes24h = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Users user;
}
