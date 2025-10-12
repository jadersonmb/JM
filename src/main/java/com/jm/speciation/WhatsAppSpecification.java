package com.jm.speciation;

import com.jm.dto.WhatsAppMessageDTO;
import com.jm.entity.WhatsAppMessage;

import jakarta.persistence.criteria.Predicate;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.data.jpa.domain.Specification;

public class WhatsAppSpecification {

    public static Specification<WhatsAppMessage> search(WhatsAppMessageDTO filter, OffsetDateTime start,
            OffsetDateTime end) {
        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (Objects.nonNull(filter.getPhoneNumber()) && !filter.getPhoneNumber().isEmpty()) {
                predicates.add(builder.equal(builder.lower(root.<String>get("fromPhone")),
                        filter.getPhoneNumber().toLowerCase()));
            }

            if (start != null) {
                predicates.add(builder.greaterThanOrEqualTo(root.get("receivedAt"), start));
            }

            if (end != null) {
                predicates.add(builder.lessThan(root.get("receivedAt"), end));
            }

            return builder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
