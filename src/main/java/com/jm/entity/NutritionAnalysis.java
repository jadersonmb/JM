package com.jm.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "nutrition_analysis")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NutritionAnalysis {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator",
            parameters = @Parameter(name = "uuid_gen_strategy_class", value = "org.hibernate.id.uuid.CustomVersionOneStrategy")
    )
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id", nullable = false, unique = true)
    private WhatsAppMessage message;

    @Column(name = "food_name")
    private String foodName;

    @Column(name = "calories")
    private BigDecimal calories;

    @Column(name = "protein_g")
    private BigDecimal protein;

    @Column(name = "carbs_g")
    private BigDecimal carbs;

    @Column(name = "fat_g")
    private BigDecimal fat;

    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    @Column(name = "categories_json", columnDefinition = "TEXT")
    private String categoriesJson;

    @Column(name = "confidence")
    private BigDecimal confidence;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "calories_unit_id")
    private MeasurementUnits caloriesUnit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "protein_unit_id")
    private MeasurementUnits proteinUnit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "carbs_unit_id")
    private MeasurementUnits carbsUnit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fat_unit_id")
    private MeasurementUnits fatUnit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "primary_category_id")
    private FoodCategory primaryCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meal_id")
    private Meal meal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_id")
    private Food food;

    @Column(name = "liquid_volume")
    private BigDecimal liquidVolume;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "liquid_unit_id")
    private MeasurementUnits liquidUnit;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @jakarta.persistence.PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }
}

