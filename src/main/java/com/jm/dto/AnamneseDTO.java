package com.jm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnamneseDTO {

    private UUID id;
    private UUID userId;
    private UUID countryId;
    private String countryName;
    private UUID cityId;
    private String cityName;
    private UUID educationLevelId;
    private UUID professionId;

    // Dados pessoais
    private String paciente;
    private String endereco;
    private LocalDate dataNascimento;
    private Integer idade;
    private String telefone;
    private String escolaridade;
    private String profissao;
    private String objetivoConsulta;

    // Avaliação clínica
    private String mucosa;
    private String membros;
    private String cirurgias;
    private String avaliacaoObservacoes;

    // Patologias
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

    // Bioimpedância
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

    // Avaliação bioquímica
    @Builder.Default
    private List<ExameBioquimicoDTO> examesBioquimicos = new ArrayList<>();

    // Hábitos
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

    // Preferências
    private String alimentosPreferidos;
    private String alimentosNaoGosta;

    // Recordatório 24h
    @Builder.Default
    private List<Refeicao24hDTO> refeicoes24h = new ArrayList<>();

    // Diagnóstico
    private String diagnostico;
    private String resumoDieta;
}
