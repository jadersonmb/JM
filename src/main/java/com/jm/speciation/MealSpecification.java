package com.jm.speciation;

import com.jm.entity.Meal;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public final class MealSpecification {

    private MealSpecification() {
    }

    public static Specification<Meal> search(String name, String code, String language) {
        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(name)) {
                predicates.add(builder.like(builder.lower(root.get("name")),
                        "%" + name.toLowerCase() + "%"));
            }

            if (StringUtils.hasText(code)) {
                predicates.add(builder.like(builder.lower(root.get("code")),
                        "%" + code.toLowerCase() + "%"));
            }

            if (StringUtils.hasText(language)) {
                predicates.add(builder.or(
                        builder.equal(builder.lower(root.get("language")), language.toLowerCase()),
                        builder.isNull(root.get("language"))));
            }

            return builder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
