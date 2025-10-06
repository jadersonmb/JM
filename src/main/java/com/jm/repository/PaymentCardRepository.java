package com.jm.repository;

import com.jm.entity.PaymentCard;
import com.jm.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentCardRepository extends JpaRepository<PaymentCard, UUID> {

    List<PaymentCard> findByCustomerOrderByDefaultCardDescCreatedAtDesc(Users customer);

    Optional<PaymentCard> findFirstByCustomerAndDefaultCardIsTrue(Users customer);
}
