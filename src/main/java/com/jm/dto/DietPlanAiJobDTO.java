package com.jm.dto;

import com.jm.enums.DietPlanAiJobStatus;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class DietPlanAiJobDTO {
    UUID id;
    DietPlanAiJobStatus status;
    String errorMessage;
    UUID dietPlanId;
    DietPlanDTO diet;
    OffsetDateTime createdAt;
    OffsetDateTime updatedAt;
}
