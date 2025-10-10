package com.jm.speciation;

import com.jm.dto.NutritionGoalTemplateDTO;
import com.jm.entity.NutritionGoalTemplate;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class NutritionGoalTemplateSpecification {

    private NutritionGoalTemplateSpecification() {
    }

    public static Specification<NutritionGoalTemplate> search(NutritionGoalTemplateDTO filter) {
        NutritionGoalTemplateDTO criteria = filter != null ? filter : new NutritionGoalTemplateDTO();
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

            return builder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
