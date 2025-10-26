package com.jm.dto;

import com.jm.enums.DietPlanAiSuggestionStatus;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DietPlanAiSuggestionResponse {

    private UUID requestId;
    private DietPlanAiSuggestionStatus status;
    private DietPlanDTO diet;
    private String errorMessage;
}
