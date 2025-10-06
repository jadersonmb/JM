package com.jm.dto.payment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WebhookEventRequest {

    @NotBlank
    private String provider;

    @NotNull
    private String payload;
}
