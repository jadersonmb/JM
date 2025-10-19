package com.jm.controllers;

import com.jm.dto.payment.ConfirmPaymentRequest;
import com.jm.dto.payment.PaymentFilterRequest;
import com.jm.dto.payment.PaymentIntentRequest;
import com.jm.dto.payment.PaymentIntentResponse;
import com.jm.dto.payment.PaymentResponse;
import com.jm.dto.payment.PixPaymentRequest;
import com.jm.dto.payment.PixPaymentResponse;
import com.jm.dto.payment.PaymentRecurringRequest;
import com.jm.dto.payment.PaymentRecurringResponse;
import com.jm.dto.payment.RefundRequest;
import com.jm.security.annotation.PermissionRequired;
import com.jm.services.payment.PaymentService;
import com.stripe.exception.StripeException;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/api/payments")
@RequiredArgsConstructor
@Validated
public class PaymentController {

    private final PaymentService paymentService;

    @PermissionRequired("ROLE_PAYMENTS_CREATE")
    @PostMapping("/create-intent")
    public ResponseEntity<PaymentIntentResponse> createPaymentIntent(@Valid @RequestBody PaymentIntentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.createPaymentIntent(request));
    }

    @PermissionRequired("ROLE_PAYMENTS_UPDATE")
    @PostMapping("/confirm")
    public ResponseEntity<PaymentIntentResponse> confirmPayment(@Valid @RequestBody ConfirmPaymentRequest request) {
        return ResponseEntity.ok(paymentService.confirmPayment(request));
    }

    @PermissionRequired("ROLE_PAYMENTS_CREATE")
    @PostMapping("/pix")
    public ResponseEntity<PixPaymentResponse> createPixPayment(@Valid @RequestBody PixPaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.createPixPayment(request));
    }

    @PostMapping("/subscription")
    public ResponseEntity<PaymentRecurringResponse> createSubscription(
            @Valid @RequestBody PaymentRecurringRequest request) throws StripeException {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.createSubscription(request));
    }

    @GetMapping("/subscription")
    public ResponseEntity<List<PaymentRecurringResponse>> listSubscriptions(@RequestParam UUID customerId) {
        return ResponseEntity.ok(paymentService.listSubscriptions(customerId));
    }

    @PermissionRequired("ROLE_PAYMENTS_DELETE")
    @DeleteMapping("/subscription/{id}")
    public ResponseEntity<Void> cancelSubscription(@PathVariable UUID id) {
        paymentService.cancelSubscription(id);
        return ResponseEntity.noContent().build();
    }

    @PermissionRequired("ROLE_PAYMENTS_READ")
    @GetMapping
    public Page<PaymentResponse> listPayments(@ModelAttribute PaymentFilterRequest filter, Pageable pageable) {
        return paymentService.searchPayments(pageable, filter);
    }

    @PermissionRequired("ROLE_PAYMENTS_READ")
    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable UUID id) {
        return ResponseEntity.ok(paymentService.getPayment(id));
    }

    @PermissionRequired("ROLE_PAYMENTS_UPDATE")
    @PostMapping("/{id}/refund")
    public ResponseEntity<PaymentResponse> refundPayment(@PathVariable UUID id,
            @Valid @RequestBody RefundRequest request) {
        return ResponseEntity.ok(paymentService.refundPayment(id, request));
    }
}
