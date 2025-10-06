package com.jm.repository;

import com.jm.entity.PaymentWebhook;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PaymentWebhookRepository extends JpaRepository<PaymentWebhook, UUID> {

    List<PaymentWebhook> findByProcessedFalse();
}
