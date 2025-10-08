package com.jm.speciation;

import com.jm.entity.Profession;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class ProfessionSpecification {

    private ProfessionSpecification() {
    }

    public static Specification<Profession> search(String name, String code, String language) {
        Specification<Profession> spec = Specification.where(null);
        if (StringUtils.hasText(name)) {
            String like = "%" + name.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("name")), like));
        }
        if (StringUtils.hasText(code)) {
            String like = "%" + code.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("code")), like));
        }
        if (StringUtils.hasText(language)) {
            spec = spec.and((root, query, cb) -> cb.equal(cb.lower(root.get("language")), language.trim().toLowerCase()));
        }
        return spec;
    }
}
