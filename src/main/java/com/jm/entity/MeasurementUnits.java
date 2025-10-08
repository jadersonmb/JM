package com.jm.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "measurement_units")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MeasurementUnits {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator", parameters = {
            @Parameter(name = "uuid_gen_strategy_class", value = "org.hibernate.id.uuid.CustomVersionOneStrategy")
    })
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(unique = true, nullable = false, length = 20)
    private String code;

    @Column(nullable = false, length = 10)
    private String symbol;

    @Column(name = "unit_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private UnitType unitType;

    @Column(name = "conversion_factor")
    private Double conversionFactor; // Fator de conversão para unidade base

    @Column(name = "base_unit")
    private Boolean baseUnit = false; // Se é a unidade base para conversões

    @Column(length = 100)
    private String description;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "language", length = 5)
    private String language;

    public enum UnitType {
        WEIGHT, // Peso: g, kg, mg, oz, lb
        VOLUME, // Volume: ml, l, cup, tsp, tbsp
        LENGTH, // Comprimento: cm, m, in
        COUNT, // Contagem: unit, slice, piece
        ENERGY, // Energia: kcal, kJ
        PERCENTAGE, // Porcentagem: %
        OTHER // Outros
    }
}