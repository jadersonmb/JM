package com.jm.services.payment;

import com.jm.configuration.config.PaymentGatewayProperties;
import com.jm.dto.payment.PixPaymentRequest;
import com.jm.dto.payment.RecurringChargeResponse;
import com.jm.dto.payment.RefundRequest;
import com.jm.dto.payment.PaymentRecurringRequest;
import com.jm.dto.payment.PixChargeResponse;
import com.jm.entity.PaymentPlan;
import com.jm.entity.Users;
import com.jm.enums.PaymentStatus;
import com.jm.enums.RecurringInterval;
import com.jm.enums.RecurringStatus;
import com.jm.execption.PaymentIntegrationException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class AsaasPaymentGateway {

    private final PaymentGatewayProperties properties;
    @Qualifier("asaasWebClient")
    private final WebClient webClient;
    private final PaymentPlanService paymentPlanService;

    public PixChargeResponse createPixCharge(PixPaymentRequest request, Users customer) {
        assertConfigured();
        String asaasCustomerId = "asaasCustomerId";
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("customer", asaasCustomerId);
            payload.put("billingType", "PIX");
            payload.put("value", request.getAmount().setScale(2, RoundingMode.HALF_UP));
            payload.put("description",
                    StringUtils.hasText(request.getDescription()) ? request.getDescription() : "PIX charge");
            payload.put("dueDate", LocalDate.now().plusDays(1).toString());
            payload.put("externalReference", customer.getId().toString());

            if (StringUtils.hasText(properties.getAsaas().getPixKey())) {
                payload.put("pixKey", properties.getAsaas().getPixKey());
            }

            Map response = webClient.post().uri("/payments").contentType(MediaType.APPLICATION_JSON).bodyValue(payload)
                    .retrieve().bodyToMono(Map.class).block();

            if (response == null) {
                throw new PaymentIntegrationException("Empty response from Asaas while creating PIX charge");
            }

            Map pix = (Map) response.get("pixQrCode");
            String gatewayId = String.valueOf(response.get("id"));
            String status = String.valueOf(response.getOrDefault("status", "PENDING"));
            String qrImage = pix != null ? String.valueOf(pix.get("encodedImage")) : null;
            String payloadString = pix != null ? String.valueOf(pix.get("payload")) : null;
            OffsetDateTime expiresAt = pix != null && pix.get("expirationDate") != null
                    ? OffsetDateTime.parse(String.valueOf(pix.get("expirationDate")))
                    : OffsetDateTime.now(ZoneOffset.UTC).plusMinutes(30);

            return PixChargeResponse.builder()
                    .gatewayChargeId(gatewayId)
                    .pixKey(properties.getAsaas().getPixKey())
                    .qrCodeImage(qrImage)
                    .payload(payloadString)
                    .status(mapPaymentStatus(status))
                    .amount(request.getAmount())
                    .expiresAt(expiresAt)
                    .build();
        } catch (WebClientResponseException ex) {
            log.error("Asaas PIX charge failed: {}", ex.getResponseBodyAsString(), ex);
            throw new PaymentIntegrationException("Unable to create Asaas PIX charge", ex);
        }
    }

    public RecurringChargeResponse createSubscription(PaymentRecurringRequest request, Map<String, Object> metadata,
            Users customer) {
        assertConfigured();
        String asaasCustomerId = extractString(metadata, "asaasCustomerId");
        PaymentPlan plan = paymentPlanService.findActiveById(request.getPaymentPlanId());
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("customer", asaasCustomerId);
            payload.put("billingType", "PIX");
            payload.put("value", plan.getAmount().setScale(2, RoundingMode.HALF_UP));
            payload.put("cycle", mapInterval(plan.getIntervals()));
            payload.put("description",
                    StringUtils.hasText(plan.getDescription()) ? plan.getDescription() : plan.getName());
            payload.put("chargeType", Boolean.TRUE.equals(request.getImmediateCharge()) ? "NEXT_DUE_DATE" : "FIXED");
            payload.put("startDate", LocalDate.now().toString());
            payload.put("endDate", (Object) null);
            payload.put("externalReference", customer.getId().toString());
            if (StringUtils.hasText(plan.getAsaasPlanId())) {
                payload.put("plan", plan.getAsaasPlanId());
            }

            Map response = webClient.post().uri("/subscriptions").contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(payload).retrieve().bodyToMono(Map.class).block();

            if (response == null) {
                throw new PaymentIntegrationException("Empty response from Asaas while creating subscription");
            }

            String gatewayId = String.valueOf(response.get("id"));
            String status = String.valueOf(response.getOrDefault("status", "ACTIVE"));
            LocalDate nextDueDate = response.get("nextDueDate") != null
                    ? LocalDate.parse(String.valueOf(response.get("nextDueDate")))
                    : null;

            return RecurringChargeResponse.builder()
                    .subscriptionId(gatewayId)
                    .status(mapSubscriptionStatus(status))
                    .interval(plan.getIntervals())
                    .amount(plan.getAmount())
                    .nextBillingDate(nextDueDate)
                    .build();
        } catch (WebClientResponseException ex) {
            log.error("Asaas subscription creation failed: {}", ex.getResponseBodyAsString(), ex);
            throw new PaymentIntegrationException("Unable to create Asaas subscription", ex);
        }
    }

    public void refund(String gatewayPaymentId, RefundRequest request) {
        assertConfigured();
        try {
            Map<String, Object> payload = new HashMap<>();
            if (request.getAmount() != null) {
                payload.put("value", request.getAmount().setScale(2, RoundingMode.HALF_UP));
            }
            if (StringUtils.hasText(request.getReason())) {
                payload.put("description", request.getReason());
            }

            webClient.post().uri("/payments/{id}/refunds", gatewayPaymentId).contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(payload).retrieve().toBodilessEntity().block();
        } catch (WebClientResponseException ex) {
            log.error("Asaas refund failed: {}", ex.getResponseBodyAsString(), ex);
            throw new PaymentIntegrationException("Unable to create Asaas refund", ex);
        }
    }

    public void cancelSubscription(String subscriptionId) {
        assertConfigured();
        try {
            webClient.delete().uri("/subscriptions/{id}", subscriptionId).retrieve().toBodilessEntity().block();
        } catch (WebClientResponseException ex) {
            log.error("Asaas subscription cancellation failed: {}", ex.getResponseBodyAsString(), ex);
            throw new PaymentIntegrationException("Unable to cancel Asaas subscription", ex);
        }
    }

    private RecurringStatus mapSubscriptionStatus(String status) {
        return switch (status.toUpperCase()) {
            case "ACTIVE", "AWAITING_CYCLE" -> RecurringStatus.ACTIVE;
            case "SUSPENDED" -> RecurringStatus.PAUSED;
            case "CANCELLED" -> RecurringStatus.CANCELLED;
            case "FINISHED" -> RecurringStatus.EXPIRED;
            default -> RecurringStatus.ACTIVE;
        };
    }

    private PaymentStatus mapPaymentStatus(String status) {
        return switch (status.toUpperCase()) {
            case "RECEIVED", "CONFIRMED" -> PaymentStatus.COMPLETED;
            case "RECEIVED_IN_CASH", "RECEIVED_IN_PROTEST" -> PaymentStatus.COMPLETED;
            case "PENDING", "AWAITING_RISK_ANALYSIS" -> PaymentStatus.PENDING;
            case "OVERDUE", "REFUSED", "CANCELLED" -> PaymentStatus.FAILED;
            default -> PaymentStatus.PENDING;
        };
    }

    private String mapInterval(RecurringInterval interval) {
        return switch (interval) {
            case DAILY -> "DAILY";
            case WEEKLY -> "WEEKLY";
            case MONTHLY -> "MONTHLY";
            case QUARTERLY -> "QUARTERLY";
            case SEMI_ANNUAL -> "SEMIANNUAL";
            case YEARLY -> "YEARLY";
        };
    }

    private void assertConfigured() {
        Assert.isTrue(StringUtils.hasText(properties.getAsaas().getApiKey()), "Asaas API key is not configured");
    }

    private String extractString(Map<String, Object> metadata, String key) {
        if (metadata == null || metadata.get(key) == null) {
            throw new PaymentIntegrationException("Missing required metadata: " + key);
        }
        return String.valueOf(metadata.get(key));
    }
}
