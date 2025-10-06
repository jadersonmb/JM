package com.jm.dto.payment;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class PixPaymentRequest {

    @NotNull
    private UUID customerId;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal amount;

    @Size(max = 255)
    private String description;

}
