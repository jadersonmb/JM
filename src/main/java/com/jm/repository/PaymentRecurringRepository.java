package com.jm.repository;

import com.jm.entity.PaymentRecurring;
import com.jm.entity.Users;
import com.jm.enums.RecurringStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRecurringRepository extends JpaRepository<PaymentRecurring, UUID> {

    List<PaymentRecurring> findByCustomer(Users customer);

    List<PaymentRecurring> findByStatus(RecurringStatus status);

    Optional<PaymentRecurring> findByGatewaySubscriptionId(String gatewaySubscriptionId);

    @Query("""
            select case when count(r) > 0 then true else false end
            from PaymentRecurring r
            where r.customer.id = :customerId
              and r.status = :status
              and (r.nextBillingDate is null or r.nextBillingDate >= :referenceDate)
            """)
    boolean existsActiveByCustomerAndStatus(@Param("customerId") UUID customerId,
            @Param("status") RecurringStatus status,
            @Param("referenceDate") LocalDate referenceDate);
}
