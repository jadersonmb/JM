package com.jm.speciation;

import com.jm.dto.DietPlanDTO;
import com.jm.entity.DietMeal;
import com.jm.entity.DietPlan;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DietPlanSpecification {

    private DietPlanSpecification() {
    }

    public static Specification<DietPlan> search(DietPlanDTO filter) {
        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter == null) {
                return builder.conjunction();
            }

            if (StringUtils.hasText(filter.getPatientName())) {
                predicates.add(builder.like(builder.lower(root.get("patientName")),
                        "%".concat(filter.getPatientName().toLowerCase()).concat("%")));
            }

            if (Objects.nonNull(filter.getCreatedByUserId())) {
                predicates.add(builder.equal(root.get("createdByUserId"), filter.getCreatedByUserId()));
            }

            if (Objects.nonNull(filter.getActive())) {
                predicates.add(builder.equal(root.get("active"), filter.getActive()));
            }

            if (Objects.nonNull(filter.getDayOfWeek())) {
                predicates.add(builder.equal(root.get("dayOfWeek"), filter.getDayOfWeek()));
            }

            if (Objects.nonNull(filter.getMealType())) {
                Join<DietPlan, DietMeal> meals = root.join("meals", JoinType.LEFT);
                predicates.add(builder.equal(meals.get("mealType"), filter.getMealType()));
                query.distinct(true);
            }

            return builder.and(predicates.toArray(new Predicate[0]));
        };
    }
}

