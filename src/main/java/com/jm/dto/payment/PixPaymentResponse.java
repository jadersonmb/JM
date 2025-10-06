package com.jm.dto.payment;

import com.jm.enums.PaymentStatus;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Value
@Builder
public class PixPaymentResponse {
    UUID id;
    String paymentId;
    String qrCodeImage;
    String payload;
    String pixKey;
    BigDecimal amount;
    PaymentStatus status;
    OffsetDateTime expiresAt;
}
