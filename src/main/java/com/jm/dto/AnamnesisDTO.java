package com.jm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnamnesisDTO {

    private UUID id;
    private UUID userId;
    private UUID countryId;
    private String countryName;
    private UUID cityId;
    private String cityName;
    private UUID educationLevelId;
    private UUID professionId;

    private String patientName;
    private String address;
    private LocalDate birthDate;
    private Integer age;
    private String phoneNumber;
    private String educationLevelName;
    private String professionName;
    private String consultationGoal;

    private String mucosa;
    private String limbs;
    private String surgicalHistory;
    private String clinicalNotes;

    private BigDecimal weightKg;
    private BigDecimal heightCm;
    private BigDecimal bodyMassIndex;
    private BigDecimal bodyFatPercentage;
    private BigDecimal muscleMassPercentage;
    private BigDecimal basalMetabolicRate;
    private BigDecimal abdominalCircumference;
    private BigDecimal waistCircumference;
    private BigDecimal hipCircumference;
    private BigDecimal armCircumference;
    private BigDecimal kneeCircumference;
    private BigDecimal thoraxCircumference;

    @Builder.Default
    private Set<UUID> pathologyIds = new HashSet<>();

    @Builder.Default
    private List<AnamnesisBiochemicalResultDTO> biochemicalResults = new ArrayList<>();

    private String mealPreparation;
    private String mealLocation;
    private String workSchedule;
    private String studySchedule;
    private String appetite;
    private String waterIntake;
    private String physicalActivity;
    private String activityFrequency;
    private String activityDuration;
    private Boolean smokes;
    private Boolean drinksAlcohol;
    private String supplements;
    private String sleepQuality;
    private String chewingQuality;
    private String habitNotes;

    @Builder.Default
    private List<AnamnesisFoodPreferenceDTO> foodPreferences = new ArrayList<>();

    @Builder.Default
    private List<AnamnesisFoodRecallDTO> foodRecalls = new ArrayList<>();

    private String diagnosis;
    private String dietSummary;
}
