package com.jm.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import com.jm.enums.OllamaStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ollama")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Ollama {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator", parameters = {
            @Parameter(name = "uuid_gen_strategy_class", value = "org.hibernate.id.uuid.CustomVersionOneStrategy")
    })
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /** Modelo usado no Ollama ex: llama3.2, mistral, phi3 */
    @Column(name = "model", nullable = false)
    private String model;

    @Column(name = "from_of", nullable = false)
    private String from;

    /** Prompt enviado (texto ou resumo do prompt original) */
    @Column(name = "prompt", columnDefinition = "TEXT")
    private String prompt;

    /** Resposta do Ollama */
    @Column(name = "response", columnDefinition = "TEXT")
    private String response;

    /** Status do processamento: PENDING / PROCESSING / DONE / ERROR */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30)
    private OllamaStatus status;

    /** Mensagem de erro, se houver */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    /** Tempo de início da solicitação */
    @Column(name = "started_at")
    private LocalDateTime startedAt;

    /** Tempo de término da solicitação */
    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    /** Tempo total de resposta (em milissegundos) */
    @Column(name = "elapsed_ms")
    private Long elapsedMs;

    /** Usuário que fez a requisição (se quiser rastrear) */
    @Column(name = "requested_by")
    private UUID requestedBy;

    /** Lista de imagens (se houver) em Base64 ou URL */
    @Column(name = "images", columnDefinition = "TEXT")
    private String images;

    /** Dados auxiliares para processamento do request */
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private Users user;

}