package com.jm.services.payment;

import com.jm.configuration.config.PaymentGatewayProperties;
import com.jm.dto.payment.GatewayPaymentResponse;
import com.jm.dto.payment.PaymentRecurringRequest;
import com.jm.dto.payment.RecurringChargeResponse;
import com.jm.dto.payment.RefundRequest;
import com.jm.entity.PaymentCard;
import com.jm.entity.PaymentPlan;
import com.jm.entity.Users;
import com.jm.enums.PaymentStatus;
import com.jm.enums.RecurringStatus;
import com.jm.execption.PaymentIntegrationException;
import com.jm.mappers.UserMapper;
import com.jm.services.UserService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Price;
import com.stripe.model.Product;
import com.stripe.model.Refund;
import com.stripe.model.Subscription;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.PaymentIntentConfirmParams;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.PriceCreateParams;
import com.stripe.param.ProductCreateParams;
import com.stripe.param.RefundCreateParams;
import com.stripe.param.SubscriptionCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class StripePaymentGateway {

    private final PaymentGatewayProperties properties;
    private final PaymentPlanService paymentPlanService;
    private final UserService userService;
    private final UserMapper userMapper;

    @Value("${payments.stripe.secret-key}")
    private String stripeSecretKey;

    @PostConstruct
    void init() {
        if (StringUtils.hasText(properties.getStripe().getSecretKey())) {
            Stripe.apiKey = properties.getStripe().getSecretKey();
        } else {
            log.warn("Stripe secret key is not configured. Stripe integration is disabled.");
        }
    }

    public GatewayPaymentResponse createCardPayment(BigDecimal amount, String currency, String description,
            Users customer, PaymentCard card, Map<String, Object> metadata) {
        assertStripeConfigured();
        try {
            PaymentIntentCreateParams.Builder builder = PaymentIntentCreateParams.builder()
                    .setAmount(toStripeAmount(amount)).setCurrency(currency.toLowerCase())
                    .setConfirmationMethod(PaymentIntentCreateParams.ConfirmationMethod.AUTOMATIC)
                    .setConfirm(Boolean.TRUE).addPaymentMethodType("card");

            if (StringUtils.hasText(description)) {
                builder.setDescription(description);
            }

            if (card != null) {
                builder.setPaymentMethod("pm_card_visa"); /* card.getCardToken() */
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

    public RecurringChargeResponse createSubscription(PaymentRecurringRequest recurringPaymentRequest,
            PaymentCard paymentMethod, Users customer, Map<String, Object> metadata) throws StripeException {
        assertStripeConfigured();
        PaymentPlan plan = paymentPlanService.findActiveById(recurringPaymentRequest.getPaymentPlanId());

        if (Objects.isNull(plan.getStripePriceId())) {
            ProductCreateParams productParams = ProductCreateParams.builder()
                    .setName(plan.getName())
                    .setDescription(plan.getDescription())
                    .putMetadata("plan_code", plan.getIntervals().name())
                    .build();

            Product productPlanStripe = Product.create(productParams);

            PriceCreateParams priceParams = PriceCreateParams.builder()
                    .setProduct(productPlanStripe.getId())
                    .setCurrency(plan.getCurrency())
                    .setUnitAmount(plan.getAmount().multiply(BigDecimal.valueOf(100)).longValue())
                    .setRecurring(PriceCreateParams.Recurring.builder()
                            .setInterval(PriceCreateParams.Recurring.Interval.MONTH) /* Modificar para pegar do plan */
                            .build())
                    .putMetadata("plan_code", plan.getIntervals().name())
                    .build();

            Price price = Price.create(priceParams);
            plan.setStripePriceId(price.getId());
            paymentPlanService.save(plan);
        }

        findOrCreateStripeCustomer(customer, paymentMethod.getCardToken()); // paymentMethodStripe.getId()

        Assert.hasText(customer.getStripeCustomerId(), "Stripe customer id is required for subscriptions");
        Assert.notNull(paymentMethod, "Payment method is required for subscriptions");
        Assert.hasText(plan.getStripePriceId(), "Stripe price id is required for the selected plan");
        try {

            SubscriptionCreateParams params = SubscriptionCreateParams.builder()
                    .setCustomer(customer.getStripeCustomerId())
                    .addItem(
                            SubscriptionCreateParams.Item.builder()
                                    .setPrice(plan.getStripePriceId())
                                    .build())
                    .build();

            Subscription subscription = Subscription.create(params);

            return RecurringChargeResponse.builder()
                    .subscriptionId(subscription.getId())
                    .status(mapSubscriptionStatus(subscription.getStatus()))
                    .interval(plan.getIntervals())
                    .amount(plan.getAmount())
                    /*
                     * .nextBillingDate(subscription.getCurrentPeriodEnd() != null ? Instant
                     * .ofEpochSecond(subscription.getCurrentPeriodEnd()).atZone(ZoneOffset.UTC).
                     * toLocalDate() : null)
                     */
                    .build();
        } catch (StripeException ex) {
            log.error("Stripe subscription creation failed", ex);
            throw new PaymentIntegrationException("Unable to create Stripe subscription", ex);
        }
    }

    public void refund(String gatewayPaymentId, RefundRequest request) {
        assertStripeConfigured();
        try {
            RefundCreateParams.Builder builder = RefundCreateParams.builder().setPaymentIntent(gatewayPaymentId);

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
        return amount.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP).longValueExact();
    }

    private void assertStripeConfigured() {
        if (!StringUtils.hasText(properties.getStripe().getSecretKey())) {
            throw new PaymentIntegrationException("Stripe secret key is not configured");
        }
    }

    private Customer findOrCreateStripeCustomer(Users customer, String paymentMethodId) throws StripeException {

        if (customer.getStripeCustomerId() != null) {
            return Customer.retrieve(customer.getStripeCustomerId());
        }

        CustomerCreateParams customerParams = CustomerCreateParams.builder()
                .setEmail(customer.getEmail())
                .setName(customer.getName())
                .setPaymentMethod(paymentMethodId)
                .setInvoiceSettings(
                        CustomerCreateParams.InvoiceSettings.builder()
                                .setDefaultPaymentMethod(paymentMethodId)
                                .build())
                .build();

        Customer customerStripe = Customer.create(customerParams);

        customer.setStripeCustomerId(customerStripe.getId());
        userService.updateUser(userMapper.toDTO(customer));

        return customerStripe;
    }
}
