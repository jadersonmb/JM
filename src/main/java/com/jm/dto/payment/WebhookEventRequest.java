package com.jm.dto.payment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

@Data
public class WebhookEventRequest {

    @NotBlank
    private String provider;

    @NotNull
    private Map<String, Object> payload;
}
