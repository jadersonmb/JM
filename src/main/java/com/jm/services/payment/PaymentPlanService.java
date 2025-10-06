package com.jm.services.payment;

import com.jm.dto.payment.PaymentPlanResponse;
import com.jm.entity.PaymentPlan;
import com.jm.repository.PaymentPlanRepository;
import lombok.RequiredArgsConstructor;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentPlanService {

    private final PaymentPlanRepository paymentPlanRepository;

    public List<PaymentPlanResponse> listActivePlans() {
        return paymentPlanRepository.findByActiveTrueOrderByAmountAsc().stream().map(this::toResponse)
                .collect(Collectors.toList());
    }

    public PaymentPlan findActiveById(UUID id) {
        return paymentPlanRepository.findById(id).filter(PaymentPlan::getActive)
                .orElseThrow(() -> new EntityNotFoundException("Payment plan not found or inactive"));
    }

    public PaymentPlanResponse toResponse(PaymentPlan plan) {
        return PaymentPlanResponse.builder()
                .id(plan.getId())
                .code(plan.getCode())
                .name(plan.getName())
                .description(plan.getDescription())
                .amount(plan.getAmount())
                .currency(plan.getCurrency())
                .interval(plan.getIntervals())
                .stripePriceId(plan.getStripePriceId())
                .asaasPlanId(plan.getAsaasPlanId())
                .build();
    }
}
