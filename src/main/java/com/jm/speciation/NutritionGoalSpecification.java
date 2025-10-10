package com.jm.speciation;

import com.jm.dto.NutritionGoalDTO;
import com.jm.entity.NutritionGoal;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class NutritionGoalSpecification {

    private NutritionGoalSpecification() {
    }

    public static Specification<NutritionGoal> search(NutritionGoalDTO filter) {
        NutritionGoalDTO criteria = filter != null ? filter : new NutritionGoalDTO();
        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (criteria.getType() != null) {
                predicates.add(builder.equal(root.get("type"), criteria.getType()));
            }

            if (criteria.getPeriodicity() != null) {
                predicates.add(builder.equal(root.get("periodicity"), criteria.getPeriodicity()));
            }

            if (criteria.getActive() != null) {
                predicates.add(builder.equal(root.get("active"), criteria.getActive()));
            }

            if (Objects.nonNull(criteria.getCreatedByUserId())) {
                predicates.add(builder.equal(root.join("createdBy", JoinType.LEFT).get("id"),
                        criteria.getCreatedByUserId()));
            }

            if (criteria.getTargetMode() != null) {
                predicates.add(builder.equal(root.get("targetMode"), criteria.getTargetMode()));
            }

            return builder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
