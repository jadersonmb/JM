package com.jm.dto.payment;

import com.jm.enums.PaymentMethodType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentIntentRequest {

    @NotNull
    private UUID customerId;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal amount;

    @Builder.Default
    @Size(min = 3, max = 3)
    private String currency = "BRL";

    @NotNull
    private PaymentMethodType paymentMethod;

    @Size(max = 255)
    private String description;

    private UUID paymentCardId;

    private Map<String, Object> metadata;
}
