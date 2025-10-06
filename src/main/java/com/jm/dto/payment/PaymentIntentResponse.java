package com.jm.dto.payment;

import com.jm.enums.PaymentMethodType;
import com.jm.enums.PaymentStatus;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Value
@Builder
public class PaymentIntentResponse {
    UUID id;
    String paymentId;
    PaymentStatus status;
    PaymentMethodType paymentMethod;
    BigDecimal amount;
    String currency;
    String description;
    String clientSecret;
    Map<String, Object> metadata;
    OffsetDateTime createdAt;
    OffsetDateTime updatedAt;
}
