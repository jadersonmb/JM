package com.jm.dto.payment;

import com.jm.enums.PaymentMethodType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;
import java.util.UUID;

@Data
public class PaymentRecurringRequest {

    @NotNull
    private UUID customerId;

    @NotNull
    private UUID paymentPlanId;

    private UUID paymentMethodId;

    @NotNull
    private PaymentMethodType paymentMethod;

    private Boolean immediateCharge;

    private Map<String, Object> metadata;
}
