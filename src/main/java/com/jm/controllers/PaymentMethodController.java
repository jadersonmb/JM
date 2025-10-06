package com.jm.controllers;

import com.jm.dto.payment.CardRequest;
import com.jm.dto.payment.PaymentMethodResponse;
import com.jm.services.payment.PaymentService;
import com.stripe.exception.StripeException;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/api/payment-methods")
@RequiredArgsConstructor
@Validated
public class PaymentMethodController {

    private final PaymentService paymentService;

    @PostMapping("/card")
    public ResponseEntity<PaymentMethodResponse> addCard(@Valid @RequestBody CardRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.addCard(request));
    }

    @GetMapping
    public ResponseEntity<List<PaymentMethodResponse>> listCards(@RequestParam UUID customerId) throws StripeException {
        return ResponseEntity.ok(paymentService.listCards(customerId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCard(@PathVariable UUID id, @RequestParam UUID customerId) {
        paymentService.deleteCard(id, customerId);
        return ResponseEntity.noContent().build();
    }
}
