package com.jm.services.payment;

import com.jm.enums.RecurringStatus;
import com.jm.repository.PaymentRecurringRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RecurringPaymentService {

    private final PaymentRecurringRepository recurringPaymentRepository;

    public boolean hasActiveRecurringPayment(UUID userId) {
        if (userId == null) {
            return false;
        }
        return recurringPaymentRepository.existsActiveByCustomerAndStatus(userId, RecurringStatus.ACTIVE, LocalDate.now());
    }
}
