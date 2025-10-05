package com.jm.services.payment.model;

import com.jm.enums.PaymentStatus;
import lombok.Builder;
import lombok.Value;

import java.util.Map;

@Value
@Builder
public class GatewayPaymentResponse {
    String gatewayPaymentId;
    String clientSecret;
    PaymentStatus status;
    Map<String, String> metadata;
}
