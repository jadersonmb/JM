package com.jm.repository;

import com.jm.entity.RecurringPayment;
import com.jm.entity.Users;
import com.jm.enums.RecurringStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RecurringPaymentRepository extends JpaRepository<RecurringPayment, Long> {

    List<RecurringPayment> findByCustomer(Users customer);

    List<RecurringPayment> findByStatus(RecurringStatus status);

    Optional<RecurringPayment> findByGatewaySubscriptionId(String gatewaySubscriptionId);
}
