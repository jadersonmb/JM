package com.jm.entity;

import com.jm.enums.BodyPart;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "photo_evolution")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhotoEvolution {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator", parameters = {
            @Parameter(name = "uuid_gen_strategy_class", value = "org.hibernate.id.uuid.CustomVersionOneStrategy")
    })
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "image_id", nullable = false)
    private Image image;

    @Enumerated(EnumType.STRING)
    @Column(name = "body_part", nullable = false, length = 50)
    private BodyPart bodyPart;

    @Column(name = "captured_at")
    private LocalDate capturedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "weight", precision = 6, scale = 2)
    private BigDecimal weight;

    @Column(name = "body_fat_percentage", precision = 5, scale = 2)
    private BigDecimal bodyFatPercentage;

    @Column(name = "muscle_mass", precision = 6, scale = 2)
    private BigDecimal muscleMass;

    @Column(name = "visceral_fat", precision = 5, scale = 2)
    private BigDecimal visceralFat;

    @Column(name = "waist_circumference", precision = 6, scale = 2)
    private BigDecimal waistCircumference;

    @Column(name = "hip_circumference", precision = 6, scale = 2)
    private BigDecimal hipCircumference;

    @Column(name = "chest_circumference", precision = 6, scale = 2)
    private BigDecimal chestCircumference;

    @Column(name = "left_arm_circumference", precision = 6, scale = 2)
    private BigDecimal leftArmCircumference;

    @Column(name = "right_arm_circumference", precision = 6, scale = 2)
    private BigDecimal rightArmCircumference;

    @Column(name = "left_thigh_circumference", precision = 6, scale = 2)
    private BigDecimal leftThighCircumference;

    @Column(name = "right_thigh_circumference", precision = 6, scale = 2)
    private BigDecimal rightThighCircumference;

    @Column(name = "caloric_intake", precision = 7, scale = 2)
    private BigDecimal caloricIntake;

    @Column(name = "protein_intake", precision = 6, scale = 2)
    private BigDecimal proteinIntake;

    @Column(name = "carbohydrate_intake", precision = 6, scale = 2)
    private BigDecimal carbohydrateIntake;

    @Column(name = "fat_intake", precision = 6, scale = 2)
    private BigDecimal fatIntake;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
