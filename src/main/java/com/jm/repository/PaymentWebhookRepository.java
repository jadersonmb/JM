package com.jm.repository;

import com.jm.entity.PaymentWebhook;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentWebhookRepository extends JpaRepository<PaymentWebhook, Long> {

    List<PaymentWebhook> findByProcessedFalse();
}
