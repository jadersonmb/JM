package com.jm.repository;

import com.jm.entity.WhatsAppMessage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WhatsAppMessageRepository
        extends JpaRepository<WhatsAppMessage, UUID>, JpaSpecificationExecutor<WhatsAppMessage> {
    Optional<WhatsAppMessage> findByWhatsappMessageId(String whatsappMessageId);

    List<WhatsAppMessage> findTop20ByOrderByReceivedAtDesc();
}
