package com.jm.controllers;

import com.jm.dto.payment.WebhookEventRequest;
import com.jm.services.payment.PaymentWebhookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/webhooks/payment")
@RequiredArgsConstructor
@Validated
public class WebhookController {

    private final PaymentWebhookService paymentWebhookService;

    @PostMapping
    public ResponseEntity<Void> receiveWebhook(@Valid @RequestBody WebhookEventRequest request) {
        paymentWebhookService.handleWebhook(request);
        return ResponseEntity.accepted().build();
    }
}
