package com.jm.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

@Entity
@Table(name = "anamneses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Anamnesis {

        @Id
        @GeneratedValue(generator = "UUID")
        @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator", parameters = {
                        @Parameter(name = "uuid_gen_strategy_class", value = "org.hibernate.id.uuid.CustomVersionOneStrategy")
        })
        @Column(name = "id", updatable = false, nullable = false)
        @Builder.Default
        private UUID id = UUID.randomUUID();

        private String mucosa;

        @Column(name = "limbs")
        private String limbs;

        @Column(name = "surgical_history")
        private String surgicalHistory;

        @Column(name = "clinical_notes")
        private String clinicalNotes;

        @Column(name = "weight_kg", precision = 10, scale = 2)
        private BigDecimal weightKg;

        @Column(name = "height_cm", precision = 10, scale = 2)
        private BigDecimal heightCm;

        @Column(name = "body_mass_index", precision = 10, scale = 2)
        private BigDecimal bodyMassIndex;

        @Column(name = "body_fat_percentage", precision = 5, scale = 2)
        private BigDecimal bodyFatPercentage;

        @Column(name = "muscle_mass_percentage", precision = 5, scale = 2)
        private BigDecimal muscleMassPercentage;

        @Column(name = "basal_metabolic_rate", precision = 10, scale = 2)
        private BigDecimal basalMetabolicRate;

        @Column(name = "abdominal_circumference", precision = 10, scale = 2)
        private BigDecimal abdominalCircumference;

        @Column(name = "waist_circumference", precision = 10, scale = 2)
        private BigDecimal waistCircumference;

        @Column(name = "hip_circumference", precision = 10, scale = 2)
        private BigDecimal hipCircumference;

        @Column(name = "arm_circumference", precision = 10, scale = 2)
        private BigDecimal armCircumference;

        @Column(name = "knee_circumference", precision = 10, scale = 2)
        private BigDecimal kneeCircumference;

        @Column(name = "thorax_circumference", precision = 10, scale = 2)
        private BigDecimal thoraxCircumference;

        @Column(name = "meal_preparation")
        private String mealPreparation;

        @Column(name = "meal_location")
        private String mealLocation;

        @Column(name = "work_schedule")
        private String workSchedule;

        @Column(name = "study_schedule")
        private String studySchedule;

        @Column(name = "appetite")
        private String appetite;

        @Column(name = "water_intake")
        private String waterIntake;

        @Column(name = "physical_activity")
        private String physicalActivity;

        @Column(name = "activity_frequency")
        private String activityFrequency;

        @Column(name = "activity_duration")
        private String activityDuration;

        @Column(name = "smokes")
        private Boolean smokes;

        @Column(name = "drinks_alcohol")
        private Boolean drinksAlcohol;

        private String supplements;

        @Column(name = "sleep_quality")
        private String sleepQuality;

        @Column(name = "chewing_quality")
        private String chewingQuality;

        @Column(name = "habit_notes", length = 2000)
        private String habitNotes;

        @Column(length = 500)
        private String diagnosis;

        @Column(name = "diet_summary", length = 4000)
        private String dietSummary;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "user_id")
        private Users user;

        @ManyToMany(fetch = FetchType.LAZY)
        @JoinTable(name = "anamnesis_pathologies", joinColumns = @JoinColumn(name = "anamnesis_id"), inverseJoinColumns = @JoinColumn(name = "pathology_id"))
        @Builder.Default
        private Set<Pathology> pathologies = new HashSet<>();

        @OneToMany(mappedBy = "anamnesis", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
        @Builder.Default
        private List<AnamnesisBiochemicalResult> biochemicalResults = new ArrayList<>();

        @OneToMany(mappedBy = "anamnesis", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
        @Builder.Default
        private List<AnamnesisFoodPreference> foodPreferences = new ArrayList<>();

        @OneToMany(mappedBy = "anamnesis", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
        @Builder.Default
        private List<AnamnesisFoodRecall> foodRecalls = new ArrayList<>();
}
