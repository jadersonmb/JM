package com.jm.dto.payment;

import lombok.Builder;
import lombok.Value;

import java.time.OffsetDateTime;
import java.util.UUID;

@Value
@Builder
public class PaymentMethodResponse {
    UUID id;
    String brand;
    String lastFour;
    Integer expiryMonth;
    Integer expiryYear;
    Boolean defaultCard;
    OffsetDateTime createdAt;
}
