package com.jm.speciation;

import com.jm.dto.payment.PaymentFilterRequest;
import com.jm.entity.Payment;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public final class PaymentSpecification {

    private PaymentSpecification() {
    }

    public static Specification<Payment> applyFilters(PaymentFilterRequest filter) {
        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter == null) {
                return builder.conjunction();
            }

            if (filter.getStatus() != null) {
                predicates.add(builder.equal(root.get("paymentStatus"), filter.getStatus()));
            }

            if (filter.getPaymentMethod() != null) {
                predicates.add(builder.equal(root.get("paymentMethod"), filter.getPaymentMethod()));
            }

            if (filter.getStartDate() != null) {
                predicates.add(builder.greaterThanOrEqualTo(root.get("createdAt"), filter.getStartDate()));
            }

            if (filter.getEndDate() != null) {
                predicates.add(builder.lessThanOrEqualTo(root.get("createdAt"), filter.getEndDate()));
            }

            if (filter.getMinAmount() != null) {
                predicates.add(builder.greaterThanOrEqualTo(root.get("amount"), filter.getMinAmount()));
            }

            if (filter.getMaxAmount() != null) {
                predicates.add(builder.lessThanOrEqualTo(root.get("amount"), filter.getMaxAmount()));
            }

            if (filter.getCustomerId() != null) {
                predicates.add(builder.equal(root.get("customer").get("id"), filter.getCustomerId()));
            }

            if (StringUtils.hasText(filter.getSearch())) {
                String likePattern = "%" + filter.getSearch().trim().toLowerCase() + "%";
                predicates.add(builder.or(
                        builder.like(builder.lower(root.get("paymentId")), likePattern),
                        builder.like(builder.lower(root.get("description")), likePattern)));
            }

            return builder.and(predicates.toArray(Predicate[]::new));
        };
    }
}
