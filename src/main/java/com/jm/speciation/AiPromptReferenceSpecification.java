package com.jm.speciation;

import com.jm.entity.AiPromptReference;
import com.jm.enums.AiProvider;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class AiPromptReferenceSpecification {

    private AiPromptReferenceSpecification() {
    }

    public static Specification<AiPromptReference> search(String code, String name, String model, AiProvider provider,
            Boolean active, String owner) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(code)) {
                String value = "%" + code.trim().toUpperCase(Locale.ROOT) + "%";
                predicates.add(cb.like(cb.upper(root.get("code")), value));
            }

            if (StringUtils.hasText(name)) {
                String value = "%" + name.trim().toUpperCase(Locale.ROOT) + "%";
                predicates.add(cb.like(cb.upper(root.get("name")), value));
            }

            if (StringUtils.hasText(model)) {
                String value = "%" + model.trim().toUpperCase(Locale.ROOT) + "%";
                predicates.add(cb.like(cb.upper(root.get("model")), value));
            }

            if (StringUtils.hasText(owner)) {
                String value = "%" + owner.trim().toUpperCase(Locale.ROOT) + "%";
                predicates.add(cb.like(cb.upper(root.get("owner")), value));
            }

            if (provider != null) {
                predicates.add(cb.equal(root.get("provider"), provider));
            }

            if (active != null) {
                predicates.add(cb.equal(root.get("active"), active));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
