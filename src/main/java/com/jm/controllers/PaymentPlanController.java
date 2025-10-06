package com.jm.controllers;

import com.jm.dto.payment.PaymentPlanResponse;
import com.jm.services.payment.PaymentPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/api/payment-plans")
@RequiredArgsConstructor
@Validated
public class PaymentPlanController {

    private final PaymentPlanService paymentPlanService;

    @GetMapping
    public ResponseEntity<List<PaymentPlanResponse>> listPaymentPlans() {
        return ResponseEntity.ok(paymentPlanService.listActivePlans());
    }
}
