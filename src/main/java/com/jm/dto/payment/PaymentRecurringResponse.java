package com.jm.dto.payment;

import com.jm.enums.RecurringInterval;
import com.jm.enums.RecurringStatus;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Value
@Builder
public class PaymentRecurringResponse {
    UUID id;
    String subscriptionId;
    RecurringStatus status;
    RecurringInterval interval;
    BigDecimal amount;
    LocalDate nextBillingDate;
    PaymentPlanResponse plan;
}
