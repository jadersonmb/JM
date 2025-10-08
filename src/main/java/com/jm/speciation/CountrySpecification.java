package com.jm.speciation;

import com.jm.entity.Country;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class CountrySpecification {

    private CountrySpecification() {
    }

    public static Specification<Country> search(String name, String code, String language) {
        Specification<Country> spec = Specification.where(null);
        if (StringUtils.hasText(name)) {
            String like = "%" + name.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("name")), like));
        }
        if (StringUtils.hasText(code)) {
            spec = spec.and((root, query, cb) -> cb.equal(cb.lower(root.get("code")), code.trim().toLowerCase()));
        }
        if (StringUtils.hasText(language)) {
            spec = spec.and((root, query, cb) -> cb.equal(cb.lower(root.get("language")), language.trim().toLowerCase()));
        }
        return spec;
    }
}
