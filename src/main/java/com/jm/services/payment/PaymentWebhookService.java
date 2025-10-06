package com.jm.services.payment;

import com.jm.dto.payment.WebhookEventRequest;
import com.jm.entity.PaymentWebhook;
import com.jm.enums.PaymentStatus;
import com.jm.enums.RecurringStatus;
import com.jm.repository.PaymentRecurringRepository;
import com.jm.repository.PaymentWebhookRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentWebhookService {

    private final PaymentWebhookRepository paymentWebhookRepository;
    private final PaymentService paymentService;
    private final PaymentRecurringRepository paymentRecurringRepository;

    @Transactional
    public void handleWebhook(String provider, Map<String, Object> payload) {
        String eventType = resolveEventType(provider, payload);
        PaymentWebhook webhook = PaymentWebhook.builder().eventType(eventType).payload(payload).processed(Boolean.FALSE)
                .build();

        webhook = paymentWebhookRepository.save(webhook);

        try {
            switch (provider.toLowerCase()) {
            case "stripe" -> handleStripe(eventType, payload);
            case "asaas" -> handleAsaas(eventType, payload);
            default -> log.warn("Webhook provider {} not supported", provider);
            }
            webhook.setProcessed(Boolean.TRUE);
        } catch (Exception ex) {
            log.error("Failed to process webhook {}", eventType, ex);
        } finally {
            paymentWebhookRepository.save(webhook);
        }
    }

    private void handleStripe(String eventType, Map<String, Object> payload) {
        Map<String, Object> data = cast(payload.get("data"));
        Map<String, Object> object = data != null ? cast(data.get("object")) : null;
        if (object == null) {
            log.warn("Stripe webhook {} missing payload", eventType);
            return;
        }

        if (eventType.startsWith("payment_intent") || eventType.startsWith("charge")) {
            String paymentIntentId = Optional.ofNullable(object.get("id")).map(Object::toString).orElse(null);
            if (!StringUtils.hasText(paymentIntentId)) {
                log.warn("Stripe webhook {} without payment intent id", eventType);
                return;
            }
            PaymentStatus status = switch (eventType) {
            case "payment_intent.succeeded" -> PaymentStatus.COMPLETED;
            case "payment_intent.processing" -> PaymentStatus.PROCESSING;
            case "payment_intent.payment_failed" -> PaymentStatus.FAILED;
            case "charge.refunded" -> PaymentStatus.REFUNDED;
            default -> PaymentStatus.PENDING;
            };
            paymentService.updatePaymentStatus(paymentIntentId, status, object);
        }

        if (eventType.startsWith("customer.subscription")) {
            updateRecurringFromStripe(object);
        }
    }

    private void handleAsaas(String eventType, Map<String, Object> payload) {
        Map<String, Object> payment = cast(payload.get("payment"));
        if (payment == null) {
            log.warn("Asaas webhook {} missing payment payload", eventType);
            return;
        }
        String gatewayPaymentId = Optional.ofNullable(payment.get("id")).map(Object::toString).orElse(null);
        String statusRaw = Optional.ofNullable(payment.get("status")).map(Object::toString).orElse("PENDING");
        PaymentStatus status = mapAsaasStatus(statusRaw);
        if (!StringUtils.hasText(gatewayPaymentId)) {
            log.warn("Asaas webhook {} missing payment id", eventType);
            return;
        }
        paymentService.updatePaymentStatus(gatewayPaymentId, status, payment);
    }

    private void updateRecurringFromStripe(Map<String, Object> subscription) {
        String subscriptionId = Optional.ofNullable(subscription.get("id")).map(Object::toString).orElse(null);
        if (!StringUtils.hasText(subscriptionId)) {
            log.warn("Stripe subscription webhook without id");
            return;
        }
        paymentRecurringRepository.findByGatewaySubscriptionId(subscriptionId).ifPresent(recurring -> {
            String statusRaw = Optional.ofNullable(subscription.get("status")).map(Object::toString).orElse("active");
            recurring.setStatus(mapStripeSubscriptionStatus(statusRaw));
            Object periodEnd = subscription.get("current_period_end");
            if (periodEnd instanceof Number number) {
                recurring.setNextBillingDate(
                        Instant.ofEpochSecond(number.longValue()).atZone(ZoneOffset.UTC).toLocalDate());
            }
            paymentRecurringRepository.save(recurring);
        });
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> cast(Object value) {
        if (value instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return null;
    }

    private String resolveEventType(String provider, Map<String, Object> payload) {
        if ("stripe".equalsIgnoreCase(provider)) {
            return Optional.ofNullable(payload.get("type")).map(Object::toString).orElse("stripe.unknown");
        }
        if ("asaas".equalsIgnoreCase(provider)) {
            return Optional.ofNullable(payload.get("event")).map(Object::toString).orElse("asaas.unknown");
        }
        return provider + ".unknown";
    }

    private PaymentStatus mapAsaasStatus(String status) {
        return switch (status.toUpperCase()) {
        case "RECEIVED", "RECEIVED_IN_CASH", "CONFIRMED" -> PaymentStatus.COMPLETED;
        case "PENDING", "AWAITING_RISK_ANALYSIS" -> PaymentStatus.PENDING;
        case "REFUNDED" -> PaymentStatus.REFUNDED;
        case "OVERDUE", "CANCELLED", "REJECTED" -> PaymentStatus.FAILED;
        default -> PaymentStatus.PENDING;
        };
    }

    private RecurringStatus mapStripeSubscriptionStatus(String value) {
        return switch (value) {
        case "active", "trialing" -> RecurringStatus.ACTIVE;
        case "past_due", "unpaid" -> RecurringStatus.PAUSED;
        case "canceled" -> RecurringStatus.CANCELLED;
        case "incomplete", "incomplete_expired" -> RecurringStatus.EXPIRED;
        default -> RecurringStatus.ACTIVE;
        };
    }
}
