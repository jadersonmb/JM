package com.jm.dto;

import com.jm.enums.DietMealType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DietPlanDTO {

    private UUID id;
    private UUID createdByUserId;
    private String patientName;
    private String notes;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private DietMealType mealType;
    @Builder.Default
    private List<DietMealDTO> meals = new ArrayList<>();
}

