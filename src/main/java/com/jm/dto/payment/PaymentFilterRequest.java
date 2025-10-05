package com.jm.dto.payment;

import com.jm.enums.PaymentMethodType;
import com.jm.enums.PaymentStatus;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
public class PaymentFilterRequest {
    private PaymentStatus status;
    private PaymentMethodType paymentMethod;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private OffsetDateTime startDate;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private OffsetDateTime endDate;
    @DecimalMin("0.00")
    private BigDecimal minAmount;
    @DecimalMin("0.00")
    private BigDecimal maxAmount;
    private String search;
}
