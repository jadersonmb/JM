package com.jm.speciation;

import com.jm.dto.ExerciseFilter;
import com.jm.entity.Exercise;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.Objects;

public final class ExerciseSpecification {

    private ExerciseSpecification() {
    }

    public static Specification<Exercise> search(ExerciseFilter filter) {
        return (root, query, cb) -> {
            query.distinct(true);
            var predicate = cb.conjunction();

            if (filter == null) {
                return predicate;
            }

            if (StringUtils.hasText(filter.getSearch())) {
                var pattern = "%" + filter.getSearch().trim().toLowerCase() + "%";
                var referenceJoin = root.join("reference", JoinType.LEFT);
                predicate = cb.and(predicate, cb.or(
                        cb.like(cb.lower(root.get("customName")), pattern),
                        cb.like(cb.lower(referenceJoin.get("name")), pattern),
                        cb.like(cb.lower(referenceJoin.get("code")), pattern)
                ));
            }

            if (Objects.nonNull(filter.getReferenceId())) {
                predicate = cb.and(predicate, cb.equal(root.get("reference").get("id"), filter.getReferenceId()));
            }

            if (Objects.nonNull(filter.getIntensity())) {
                predicate = cb.and(predicate, cb.equal(root.get("intensity"), filter.getIntensity()));
            }

            if (Objects.nonNull(filter.getUserId())) {
                predicate = cb.and(predicate,
                        cb.equal(root.join("user", JoinType.LEFT).get("id"), filter.getUserId()));
            }

            if (Objects.nonNull(filter.getMinDuration())) {
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("durationMinutes"), filter.getMinDuration()));
            }

            if (Objects.nonNull(filter.getMaxDuration())) {
                predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("durationMinutes"), filter.getMaxDuration()));
            }

            if (Objects.nonNull(filter.getMinCalories())) {
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("caloriesBurned"), filter.getMinCalories()));
            }

            if (Objects.nonNull(filter.getMaxCalories())) {
                predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("caloriesBurned"), filter.getMaxCalories()));
            }

            return predicate;
        };
    }
}
