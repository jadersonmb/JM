package com.jm.speciation;

import com.jm.entity.ExerciseReference;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class ExerciseReferenceSpecification {

    private ExerciseReferenceSpecification() {
    }

    public static Specification<ExerciseReference> search(String search, String language, String muscleGroup,
            String equipment) {
        Specification<ExerciseReference> spec = Specification.where(null);

        if (StringUtils.hasText(search)) {
            String like = "%" + search.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("code")), like),
                    cb.like(cb.lower(root.get("name")), like),
                    cb.like(cb.lower(root.get("description")), like)));
        }

        if (StringUtils.hasText(language)) {
            String normalized = language.trim().toLowerCase();
            spec = spec.and((root, query, cb) -> cb.equal(cb.lower(root.get("language")), normalized));
        }

        if (StringUtils.hasText(muscleGroup)) {
            String like = "%" + muscleGroup.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("muscleGroup")), like));
        }

        if (StringUtils.hasText(equipment)) {
            String like = "%" + equipment.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("equipment")), like));
        }

        return spec;
    }
}
