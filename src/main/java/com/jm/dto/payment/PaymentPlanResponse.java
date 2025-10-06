package com.jm.dto.payment;

import com.jm.enums.RecurringInterval;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.util.UUID;

@Value
@Builder
public class PaymentPlanResponse {
    UUID id;
    String code;
    String name;
    String description;
    BigDecimal amount;
    String currency;
    RecurringInterval interval;
    String stripePriceId;
    String asaasPlanId;
}
