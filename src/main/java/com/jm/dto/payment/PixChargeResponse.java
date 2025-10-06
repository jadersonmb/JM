package com.jm.dto.payment;

import com.jm.enums.PaymentStatus;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Value
@Builder
public class PixChargeResponse {
    String gatewayChargeId;
    String qrCodeImage;
    String payload;
    String pixKey;
    PaymentStatus status;
    BigDecimal amount;
    OffsetDateTime expiresAt;
}
