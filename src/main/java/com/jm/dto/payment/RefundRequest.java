package com.jm.dto.payment;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RefundRequest {

    @DecimalMin("0.01")
    private BigDecimal amount;

    @Size(max = 255)
    private String reason;
}
