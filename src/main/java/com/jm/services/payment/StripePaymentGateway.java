package com.jm.services.payment;

import com.jm.configuration.config.PaymentGatewayProperties;
import com.jm.dto.payment.RefundRequest;
import com.jm.dto.payment.RecurringPaymentRequest;
import com.jm.entity.PaymentCard;
import com.jm.entity.Users;
import com.jm.enums.PaymentStatus;
import com.jm.enums.RecurringStatus;
import com.jm.services.payment.model.GatewayPaymentResponse;
import com.jm.services.payment.model.RecurringChargeResponse;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.model.Subscription;
import com.stripe.param.PaymentIntentConfirmParams;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;
import com.stripe.param.SubscriptionCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class StripePaymentGateway {

    private final PaymentGatewayProperties properties;

    @PostConstruct
    void init() {
        if (StringUtils.hasText(properties.getStripe().getSecretKey())) {
            Stripe.apiKey = properties.getStripe().getSecretKey();
        } else {
            log.warn("Stripe secret key is not configured. Stripe integration is disabled.");
        }
    }

    public GatewayPaymentResponse createCardPayment(BigDecimal amount,
                                                    String currency,
                                                    String description,
                                                    Users customer,
                                                    PaymentCard card,
                                                    Map<String, Object> metadata) {
        assertStripeConfigured();
        try {
            PaymentIntentCreateParams.Builder builder = PaymentIntentCreateParams.builder()
                    .setAmount(toStripeAmount(amount))
                    .setCurrency(currency.toLowerCase())
                    .setConfirmationMethod(PaymentIntentCreateParams.ConfirmationMethod.MANUAL)
                    .setConfirm(Boolean.FALSE)
                    .addPaymentMethodType("card");

            if (StringUtils.hasText(description)) {
                builder.setDescription(description);
            }

            if (card != null) {
                builder.setPaymentMethod(card.getCardToken());
            }

            if (metadata != null) {
                metadata.forEach((key, value) -> builder.putMetadata(key, String.valueOf(value)));
            }

            builder.putMetadata("customer_reference", customer.getId().toString());

            PaymentIntent paymentIntent = PaymentIntent.create(builder.build());

            return GatewayPaymentResponse.builder()
                    .gatewayPaymentId(paymentIntent.getId())
                    .clientSecret(paymentIntent.getClientSecret())
                    .status(mapStatus(paymentIntent.getStatus()))
                    .metadata(paymentIntent.getMetadata())
                    .build();
        } catch (StripeException ex) {
            log.error("Stripe payment intent creation failed", ex);
            throw new PaymentIntegrationException("Unable to create Stripe payment intent", ex);
        }
    }

    public GatewayPaymentResponse confirmCardPayment(String gatewayPaymentId, String paymentMethodToken) {
        assertStripeConfigured();
        try {
            PaymentIntent intent = PaymentIntent.retrieve(gatewayPaymentId);
            PaymentIntentConfirmParams.Builder params = PaymentIntentConfirmParams.builder();
            if (StringUtils.hasText(paymentMethodToken)) {
                params.setPaymentMethod(paymentMethodToken);
            }
            PaymentIntent confirmed = intent.confirm(params.build());
            return GatewayPaymentResponse.builder()
                    .gatewayPaymentId(confirmed.getId())
                    .clientSecret(confirmed.getClientSecret())
                    .status(mapStatus(confirmed.getStatus()))
                    .metadata(confirmed.getMetadata())
                    .build();
        } catch (StripeException ex) {
            log.error("Stripe payment confirmation failed", ex);
            throw new PaymentIntegrationException("Stripe confirmation failed", ex);
        }
    }

    public RecurringChargeResponse createSubscription(RecurringPaymentRequest request,
                                                      PaymentCard paymentMethod,
                                                      String stripeCustomerId,
                                                      Map<String, Object> metadata) {
        assertStripeConfigured();
        Assert.hasText(stripeCustomerId, "Stripe customer id is required for subscriptions");
        Assert.notNull(paymentMethod, "Payment method is required for subscriptions");
        try {
            SubscriptionCreateParams.Item item = SubscriptionCreateParams.Item.builder()
                    .setPrice(request.getPlanId())
                    .build();

            SubscriptionCreateParams.Builder builder = SubscriptionCreateParams.builder()
                    .setCustomer(stripeCustomerId)
                    .addItem(item)
                    .setCollectionMethod(SubscriptionCreateParams.CollectionMethod.CHARGE_AUTOMATICALLY)
                    .setDefaultPaymentMethod(paymentMethod.getCardToken())
                    .addExpand("latest_invoice.payment_intent");

            if (!CollectionUtils.isEmpty(metadata)) {
                metadata.forEach((key, value) -> builder.putMetadata(key, String.valueOf(value)));
            }

            Subscription subscription = Subscription.create(builder.build());

            return RecurringChargeResponse.builder()
                    .subscriptionId(subscription.getId())
                    .status(mapSubscriptionStatus(subscription.getStatus()))
                    .interval(request.getInterval())
                    .amount(request.getAmount())
                    .nextBillingDate(subscription.getCurrentPeriodEnd() != null
                            ? Instant.ofEpochSecond(subscription.getCurrentPeriodEnd())
                                    .atZone(ZoneOffset.UTC)
                                    .toLocalDate()
                            : null)
                    .build();
        } catch (StripeException ex) {
            log.error("Stripe subscription creation failed", ex);
            throw new PaymentIntegrationException("Unable to create Stripe subscription", ex);
        }
    }

    public void refund(String gatewayPaymentId, RefundRequest request) {
        assertStripeConfigured();
        try {
            RefundCreateParams.Builder builder = RefundCreateParams.builder()
                    .setPaymentIntent(gatewayPaymentId);

            if (request.getAmount() != null) {
                builder.setAmount(toStripeAmount(request.getAmount()));
            }

            if (StringUtils.hasText(request.getReason())) {
                builder.setReason(RefundCreateParams.Reason.REQUESTED_BY_CUSTOMER);
            }

            Refund refund = Refund.create(builder.build());
            log.info("Stripe refund created: {} -> {}", gatewayPaymentId, refund.getStatus());
        } catch (StripeException ex) {
            log.error("Stripe refund failed", ex);
            throw new PaymentIntegrationException("Unable to create Stripe refund", ex);
        }
    }

    private PaymentStatus mapStatus(String stripeStatus) {
        return switch (stripeStatus) {
            case "requires_payment_method", "requires_confirmation", "requires_action" -> PaymentStatus.PENDING;
            case "processing" -> PaymentStatus.PROCESSING;
            case "succeeded" -> PaymentStatus.COMPLETED;
            case "canceled" -> PaymentStatus.FAILED;
            default -> PaymentStatus.PENDING;
        };
    }

    private RecurringStatus mapSubscriptionStatus(String value) {
        return switch (value) {
            case "active", "trialing" -> RecurringStatus.ACTIVE;
            case "past_due", "unpaid" -> RecurringStatus.PAUSED;
            case "canceled" -> RecurringStatus.CANCELLED;
            case "incomplete", "incomplete_expired" -> RecurringStatus.EXPIRED;
            default -> RecurringStatus.ACTIVE;
        };
    }

    private long toStripeAmount(BigDecimal amount) {
        return amount.multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact();
    }

    private void assertStripeConfigured() {
        if (!StringUtils.hasText(properties.getStripe().getSecretKey())) {
            throw new PaymentIntegrationException("Stripe secret key is not configured");
        }
    }
}

