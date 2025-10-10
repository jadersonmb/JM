package com.jm.entity;

import com.jm.enums.NutritionGoalPeriodicity;
import com.jm.enums.NutritionGoalTargetMode;
import com.jm.enums.NutritionGoalType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "nutrition_goal_template")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NutritionGoalTemplate {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator", parameters = {
            @Parameter(name = "uuid_gen_strategy_class", value = "org.hibernate.id.uuid.CustomVersionOneStrategy")
    })
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Column(name = "description", length = 255)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "goal_type", nullable = false, length = 40)
    private NutritionGoalType type;

    @Column(name = "target_value", nullable = false, precision = 12, scale = 2)
    private BigDecimal targetValue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id", nullable = false)
    private MeasurementUnits unit;

    @Enumerated(EnumType.STRING)
    @Column(name = "periodicity", nullable = false, length = 20)
    private NutritionGoalPeriodicity periodicity;

    @Column(name = "custom_period_days")
    private Integer customPeriodDays;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_mode", nullable = false, length = 20)
    private NutritionGoalTargetMode targetMode;

    @Column(name = "notes", length = 500)
    private String notes;

    @Column(name = "is_active")
    private Boolean active;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
