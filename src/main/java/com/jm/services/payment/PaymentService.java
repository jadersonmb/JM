package com.jm.services.payment;

import com.jm.dto.payment.CardRequest;
import com.jm.dto.payment.ConfirmPaymentRequest;
import com.jm.dto.payment.GatewayPaymentResponse;
import com.jm.dto.payment.PaymentFilterRequest;
import com.jm.dto.payment.PaymentIntentRequest;
import com.jm.dto.payment.PaymentIntentResponse;
import com.jm.dto.payment.PaymentMethodResponse;
import com.jm.dto.payment.PaymentResponse;
import com.jm.dto.payment.PixChargeResponse;
import com.jm.dto.payment.PixPaymentRequest;
import com.jm.dto.payment.PixPaymentResponse;
import com.jm.dto.payment.RecurringChargeResponse;
import com.jm.dto.payment.PaymentRecurringRequest;
import com.jm.entity.PaymentPlan;
import com.jm.dto.payment.PaymentRecurringResponse;
import com.jm.dto.payment.RefundRequest;
import com.jm.entity.Payment;
import com.jm.entity.PaymentCard;
import com.jm.entity.PaymentRecurring;
import com.jm.entity.Users;
import com.jm.enums.PaymentMethodType;
import com.jm.enums.PaymentStatus;
import com.jm.enums.RecurringStatus;
import com.jm.execption.PaymentIntegrationException;
import com.jm.repository.PaymentCardRepository;
import com.jm.repository.PaymentRepository;
import com.jm.repository.PaymentRecurringRepository;
import com.jm.services.UserService;
import com.jm.speciation.PaymentSpecification;
import com.stripe.exception.StripeException;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentCardRepository paymentCardRepository;
    private final PaymentRecurringRepository recurringPaymentRepository;
    private final UserService userService;
    private final PaymentPlanService paymentPlanService;
    private final StripePaymentGateway stripePaymentGateway;
    private final AsaasPaymentGateway asaasPaymentGateway;

    public PaymentIntentResponse createPaymentIntent(PaymentIntentRequest request) {
        Users customer = fetchCustomer(request.getCustomerId());
        Payment payment = Payment.builder()
                .customer(customer)
                .amount(request.getAmount())
                .currency(StringUtils.hasText(request.getCurrency()) ? request.getCurrency() : "BRL")
                .paymentMethod(request.getPaymentMethod())
                .paymentStatus(PaymentStatus.PENDING)
                .description(request.getDescription())
                .metadata(copyMetadata(request.getMetadata())).build();

        payment = paymentRepository.save(payment);

        if (request.getPaymentMethod() == PaymentMethodType.CREDIT_CARD
                || request.getPaymentMethod() == PaymentMethodType.DEBIT_CARD) {
            PaymentCard card = resolveCard(customer, request.getPaymentCardId());
            GatewayPaymentResponse gateway = stripePaymentGateway.createCardPayment(payment.getAmount(),
                    payment.getCurrency(), payment.getDescription(), customer, card, request.getMetadata());

            applyGatewayResult(payment, gateway);
        } else if (request.getPaymentMethod() == PaymentMethodType.PIX) {
            PixPaymentRequest pixRequest = PixPaymentRequest.builder()
                    .amount(payment.getAmount())
                    .description(payment.getDescription())
                    .build();
            PixChargeResponse response = asaasPaymentGateway.createPixCharge(pixRequest, customer);
            payment.setPaymentId(response.getGatewayChargeId());
            payment.setPaymentStatus(response.getStatus());
            payment.setMetadata(mergeMetadata(payment.getMetadata(),
                    Map.of("pixPayload", response.getPayload(), "pixQrCode", response.getQrCodeImage(), "pixKey",
                            response.getPixKey(), "expiresAt", String.valueOf(response.getExpiresAt()))));
        } else {
            log.warn("Payment method {} is not fully supported yet", request.getPaymentMethod());
        }

        payment = paymentRepository.save(payment);
        return toIntentResponse(payment, null);
    }

    public PaymentIntentResponse confirmPayment(ConfirmPaymentRequest request) {
        Payment payment = paymentRepository.findByPaymentId(request.getPaymentId())
                .orElseThrow(() -> new EntityNotFoundException("Payment not found"));

        if (payment.getPaymentMethod() == PaymentMethodType.CREDIT_CARD
                || payment.getPaymentMethod() == PaymentMethodType.DEBIT_CARD) {
            GatewayPaymentResponse response = stripePaymentGateway.confirmCardPayment(
                    Optional.ofNullable(request.getGatewayPaymentId()).orElse(payment.getPaymentId()),
                    request.getPaymentMethodToken());
            applyGatewayResult(payment, response);
            paymentRepository.save(payment);
            return toIntentResponse(payment, response.getClientSecret());
        }

        throw new PaymentIntegrationException("Confirmation is only supported for card payments");
    }

    public PixPaymentResponse createPixPayment(PixPaymentRequest request) {
        PaymentIntentResponse intent = createPaymentIntent(PaymentIntentRequest.builder()
                .amount(request.getAmount())
                .currency("BRL")
                .customerId(request.getCustomerId())
                .description(request.getDescription())
                .paymentMethod(PaymentMethodType.PIX)
                .build());

        Payment payment = paymentRepository.findById(intent.getId()).orElseThrow();

        Map<String, Object> metadata = payment.getMetadata();
        return PixPaymentResponse.builder()
                .id(payment.getId())
                .paymentId(payment.getPaymentId())
                .pixKey((String) metadata.get("pixKey"))
                .qrCodeImage((String) metadata.get("pixQrCode"))
                .payload((String) metadata.get("pixPayload"))
                .status(payment.getPaymentStatus())
                .amount(payment.getAmount())
                .expiresAt(parseOffsetDateTime(metadata.get("expiresAt")))
                .build();
    }

    public PaymentRecurringResponse createSubscription(PaymentRecurringRequest request) throws StripeException {
        Users customer = fetchCustomer(request.getCustomerId());
        PaymentCard card = null;
        if (request.getPaymentMethod() != PaymentMethodType.PIX) {
            card = resolveCard(customer, request.getPaymentMethodId());
        }

        RecurringChargeResponse gatewayResponse;
        if (request.getPaymentMethod() == PaymentMethodType.PIX) {
            gatewayResponse = asaasPaymentGateway.createSubscription(request, request.getMetadata(), customer);
        } else {
            gatewayResponse = stripePaymentGateway.createSubscription(request, card, customer,
                    request.getMetadata());
        }

        PaymentRecurring recurringPayment = PaymentRecurring.builder()
                .customer(customer)
                .paymentMethod(card)
                .paymentPlan(fetchPaymentPlan(request.getPaymentPlanId()))
                .interval(gatewayResponse.getInterval())
                .amount(gatewayResponse.getAmount())
                .gatewaySubscriptionId(gatewayResponse.getSubscriptionId())
                .status(Optional.ofNullable(gatewayResponse.getStatus())
                        .orElse(RecurringStatus.ACTIVE))
                .nextBillingDate(gatewayResponse.getNextBillingDate())
                .build();

        recurringPayment = recurringPaymentRepository.save(recurringPayment);

        return PaymentRecurringResponse.builder()
                .id(recurringPayment.getId())
                .subscriptionId(recurringPayment.getGatewaySubscriptionId())
                .status(recurringPayment.getStatus())
                .interval(recurringPayment.getInterval())
                .amount(recurringPayment.getAmount())
                .nextBillingDate(recurringPayment.getNextBillingDate())
                .build();
    }

    public PaymentResponse getPayment(UUID id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found"));
        return toPaymentResponse(payment);
    }

    public PaymentResponse refundPayment(UUID id, RefundRequest request) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found"));

        if (payment.getPaymentMethod() == PaymentMethodType.PIX) {
            asaasPaymentGateway.refund(payment.getPaymentId(), request);
            payment.setPaymentStatus(PaymentStatus.REFUNDED);
        } else {
            stripePaymentGateway.refund(payment.getPaymentId(), request);
            payment.setPaymentStatus(PaymentStatus.REFUNDED);
        }

        paymentRepository.save(payment);
        return toPaymentResponse(payment);
    }

    @Transactional(readOnly = true)
    public Page<PaymentResponse> searchPayments(Pageable pageable, PaymentFilterRequest filter) {
        return paymentRepository.findAll(PaymentSpecification.applyFilters(filter), pageable)
                .map(this::toPaymentResponse);
    }

    public void updatePaymentStatus(String paymentId, PaymentStatus status, Map<String, Object> metadata) {
        paymentRepository.findByPaymentId(paymentId)
                .ifPresent(payment -> {
                    payment.setPaymentStatus(status);
                    payment.setMetadata(mergeMetadata(payment.getMetadata(), metadata));
                    paymentRepository.save(payment);
                });
    }

    public PaymentMethodResponse addCard(CardRequest request) {
        Users customer = fetchCustomer(request.getCustomerId());
        if (Boolean.TRUE.equals(request.getDefaultCard())) {
            paymentCardRepository.findByCustomerOrderByDefaultCardDescCreatedAtDesc(customer).forEach(card -> {
                if (Boolean.TRUE.equals(card.getDefaultCard())) {
                    card.setDefaultCard(Boolean.FALSE);
                    paymentCardRepository.save(card);
                }
            });
        }
        PaymentCard card = PaymentCard.builder()
                .customer(customer)
                .cardToken(request.getCardToken())
                .brand(request.getBrand())
                .lastFour(request.getLastFour())
                .expiryMonth(request.getExpiryMonth())
                .expiryYear(request.getExpiryYear())
                .defaultCard(Boolean.TRUE.equals(request.getDefaultCard()))
                .build();
        card = paymentCardRepository.save(card);
        return toPaymentMethodResponse(card);
    }

    @Transactional(readOnly = true)
    public List<PaymentMethodResponse> listCards(UUID customerId) throws StripeException {
        Users customer = fetchCustomer(customerId);
        return paymentCardRepository.findByCustomerOrderByDefaultCardDescCreatedAtDesc(customer).stream()
                .map(this::toPaymentMethodResponse).toList();
    }

    public void deleteCard(UUID cardId, UUID customerId) {
        Users customer = fetchCustomer(customerId);
        PaymentCard card = paymentCardRepository.findById(cardId).filter(
                existing -> existing.getCustomer() != null && existing.getCustomer().getId().equals(customer.getId()))
                .orElseThrow(() -> new EntityNotFoundException("Card not found"));
        paymentCardRepository.delete(card);
    }

    private PaymentCard resolveCard(Users customer, UUID cardId) {
        if (Objects.nonNull(cardId)) {
            return paymentCardRepository.findById(cardId)
                    .orElseThrow(() -> new EntityNotFoundException("Card not found"));
        }

        return paymentCardRepository.findFirstByCustomerAndDefaultCardIsTrue(customer)
                .orElseThrow(() -> new EntityNotFoundException("Default card not configured"));
    }

    private Users fetchCustomer(UUID customerId) {
        return userService.findEntityById(customerId);
    }

    private PaymentPlan fetchPaymentPlan(UUID paymentPlanId) {
        return paymentPlanService.findActiveById(paymentPlanId);
    }

    private void applyGatewayResult(Payment payment, GatewayPaymentResponse gateway) {
        payment.setPaymentId(gateway.getGatewayPaymentId());
        payment.setPaymentStatus(gateway.getStatus());
        payment.setMetadata(mergeMetadata(payment.getMetadata(), gateway.getMetadata()));
    }

    private Map<String, Object> mergeMetadata(Map<String, Object> existing, Map<String, ?> additional) {
        Map<String, Object> merged = new HashMap<>();
        if (!CollectionUtils.isEmpty(existing)) {
            merged.putAll(existing);
        }
        if (!CollectionUtils.isEmpty(additional)) {
            additional.forEach((key, value) -> merged.put(key, value));
        }
        return merged;
    }

    private Map<String, Object> copyMetadata(Map<String, Object> metadata) {
        return metadata == null ? new HashMap<>() : new HashMap<>(metadata);
    }

    private PaymentIntentResponse toIntentResponse(Payment payment, String clientSecret) {
        return PaymentIntentResponse.builder()
                .id(payment.getId())
                .paymentId(payment.getPaymentId())
                .status(payment.getPaymentStatus())
                .paymentMethod(payment.getPaymentMethod())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .description(payment.getDescription())
                .clientSecret(clientSecret)
                .metadata(payment.getMetadata())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt()).build();
    }

    private PaymentResponse toPaymentResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .paymentId(payment.getPaymentId())
                .status(payment.getPaymentStatus())
                .paymentMethod(payment.getPaymentMethod())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .description(payment.getDescription())
                .metadata(payment.getMetadata())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }

    private PaymentMethodResponse toPaymentMethodResponse(PaymentCard card) {
        return PaymentMethodResponse.builder()
                .id(card.getId())
                .brand(card.getBrand())
                .lastFour(card.getLastFour())
                .expiryMonth(card.getExpiryMonth())
                .expiryYear(card.getExpiryYear())
                .defaultCard(card.getDefaultCard())
                .createdAt(card.getCreatedAt())
                .build();
    }

    private OffsetDateTime parseOffsetDateTime(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return OffsetDateTime.parse(value.toString());
        } catch (DateTimeParseException ex) {
            log.warn("Unable to parse OffsetDateTime from {}", value, ex);
            return null;
        }
    }
}
