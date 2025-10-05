package com.jm.dto.payment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Map;

@Data
public class ConfirmPaymentRequest {

    @NotBlank
    private String paymentId;

    private String gatewayPaymentId;

    private String paymentMethodToken;

    @Size(max = 255)
    private String receiptEmail;

    private Map<String, Object> metadata;
}
