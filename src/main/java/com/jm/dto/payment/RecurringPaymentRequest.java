package com.jm.dto.payment;

import com.jm.enums.PaymentMethodType;
import com.jm.enums.RecurringInterval;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Data
public class RecurringPaymentRequest {

    @NotNull
    private UUID customerId;

    @NotNull
    @Size(max = 100)
    private String planId;

    private Long paymentMethodId;

    @NotNull
    private RecurringInterval interval;

    @NotNull
    private PaymentMethodType paymentMethod;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal amount;

    private Boolean immediateCharge;

    private Map<String, Object> metadata;
}
