package com.jm.dto.payment;

import com.jm.enums.RecurringInterval;
import com.jm.enums.RecurringStatus;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;

@Value
@Builder
public class RecurringPaymentResponse {
    Long id;
    String subscriptionId;
    RecurringStatus status;
    RecurringInterval interval;
    BigDecimal amount;
    LocalDate nextBillingDate;
}
