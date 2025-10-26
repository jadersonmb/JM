package com.jm.dto;

import java.time.DayOfWeek;
import java.util.UUID;
import lombok.Data;

@Data
public class DietPlanAiSuggestionRequest {

    private UUID dietId;
    private UUID ownerId;
    private String patientName;
    private String goal;
    private String notes;
    private DayOfWeek dayOfWeek;
    private Boolean active;
}
