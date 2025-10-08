package com.jm.speciation;

import com.jm.entity.City;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class CitySpecification {

    private CitySpecification() {
    }

    public static Specification<City> search(UUID countryId, String name, String stateCode, String language) {
        Specification<City> spec = Specification.where(null);
        if (countryId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("country").get("id"), countryId));
        }
        if (StringUtils.hasText(name)) {
            String like = "%" + name.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("name")), like));
        }
        if (StringUtils.hasText(stateCode)) {
            String like = "%" + stateCode.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("stateCode")), like));
        }
        if (StringUtils.hasText(language)) {
            spec = spec.and((root, query, cb) -> cb.equal(cb.lower(root.get("language")), language.trim().toLowerCase()));
        }
        return spec;
    }
}
