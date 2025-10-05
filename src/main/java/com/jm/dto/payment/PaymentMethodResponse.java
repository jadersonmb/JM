package com.jm.dto.payment;

import lombok.Builder;
import lombok.Value;

import java.time.OffsetDateTime;

@Value
@Builder
public class PaymentMethodResponse {
    Long id;
    String brand;
    String lastFour;
    Integer expiryMonth;
    Integer expiryYear;
    Boolean defaultCard;
    OffsetDateTime createdAt;
}
