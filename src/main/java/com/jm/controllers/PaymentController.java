package com.jm.controllers;

import com.jm.dto.payment.ConfirmPaymentRequest;
import com.jm.dto.payment.PaymentFilterRequest;
import com.jm.dto.payment.PaymentIntentRequest;
import com.jm.dto.payment.PaymentIntentResponse;
import com.jm.dto.payment.PaymentResponse;
import com.jm.dto.payment.PixPaymentRequest;
import com.jm.dto.payment.PixPaymentResponse;
import com.jm.dto.payment.RecurringPaymentRequest;
import com.jm.dto.payment.RecurringPaymentResponse;
import com.jm.dto.payment.RefundRequest;
import com.jm.services.payment.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Validated
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create-intent")
    public ResponseEntity<PaymentIntentResponse> createPaymentIntent(@Valid @RequestBody PaymentIntentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.createPaymentIntent(request));
    }

    @PostMapping("/confirm")
    public ResponseEntity<PaymentIntentResponse> confirmPayment(@Valid @RequestBody ConfirmPaymentRequest request) {
        return ResponseEntity.ok(paymentService.confirmPayment(request));
    }

    @PostMapping("/pix")
    public ResponseEntity<PixPaymentResponse> createPixPayment(@Valid @RequestBody PixPaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.createPixPayment(request));
    }

    @PostMapping("/subscription")
    public ResponseEntity<RecurringPaymentResponse> createSubscription(@Valid @RequestBody RecurringPaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.createSubscription(request));
    }

    @GetMapping
    public Page<PaymentResponse> listPayments(@ModelAttribute PaymentFilterRequest filter, Pageable pageable) {
        return paymentService.searchPayments(pageable, filter);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.getPayment(id));
    }

    @PostMapping("/{id}/refund")
    public ResponseEntity<PaymentResponse> refundPayment(@PathVariable Long id, @Valid @RequestBody RefundRequest request) {
        return ResponseEntity.ok(paymentService.refundPayment(id, request));
    }
}
