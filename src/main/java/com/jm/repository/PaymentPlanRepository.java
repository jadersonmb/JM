package com.jm.repository;

import com.jm.entity.PaymentPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentPlanRepository extends JpaRepository<PaymentPlan, UUID> {

    Optional<PaymentPlan> findByCodeAndActiveTrue(String code);

    List<PaymentPlan> findByActiveTrueOrderByAmountAsc();
}
