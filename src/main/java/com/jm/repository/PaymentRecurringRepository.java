package com.jm.repository;

import com.jm.entity.PaymentRecurring;
import com.jm.entity.Users;
import com.jm.enums.RecurringStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRecurringRepository extends JpaRepository<PaymentRecurring, UUID> {

    List<PaymentRecurring> findByCustomer(Users customer);

    List<PaymentRecurring> findByStatus(RecurringStatus status);

    Optional<PaymentRecurring> findByGatewaySubscriptionId(String gatewaySubscriptionId);
}
