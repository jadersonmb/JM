package com.jm.dto;

import com.jm.enums.DietMealType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DietMealDTO {

    private UUID id;
    private UUID dietPlanId;
    private DietMealType mealType;
    private LocalTime scheduledTime;
    @Builder.Default
    private List<DietMealItemDTO> items = new ArrayList<>();
}

