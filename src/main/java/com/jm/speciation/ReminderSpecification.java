package com.jm.speciation;

import com.jm.dto.ReminderFilter;
import com.jm.entity.Reminder;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public final class ReminderSpecification {

    private ReminderSpecification() {
    }

    public static Specification<Reminder> search(ReminderFilter filter) {
        return (root, query, cb) -> {
            query.distinct(true);
            List<Predicate> predicates = new ArrayList<>();
            ReminderFilter criteria = filter != null ? filter : new ReminderFilter();

            if (StringUtils.hasText(criteria.getQuery())) {
                String like = "%" + criteria.getQuery().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), like),
                        cb.like(cb.lower(root.get("description")), like)
                ));
            }
            if (criteria.getPriority() != null) {
                predicates.add(cb.equal(root.get("priority"), criteria.getPriority()));
            }
            if (criteria.getType() != null) {
                predicates.add(cb.equal(root.get("type"), criteria.getType()));
            }
            if (criteria.getActive() != null) {
                predicates.add(cb.equal(root.get("active"), criteria.getActive()));
            }
            if (criteria.getTargetUserId() != null) {
                predicates.add(cb.equal(root.get("targetUser").get("id"), criteria.getTargetUserId()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
