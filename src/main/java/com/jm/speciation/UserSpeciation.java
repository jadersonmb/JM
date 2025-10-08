package com.jm.speciation;

import com.jm.dto.UserDTO;
import com.jm.entity.Users;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UserSpeciation {

    public static Specification<Users> search(UserDTO filter) {
        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasText(filter.getName())) {
                predicates.add(builder.like(builder.lower(root.get("name")),
                        "%".concat(filter.getName().toLowerCase()).concat("%")));
            }
            if (StringUtils.hasText(filter.getEmail())) {
                predicates.add(builder.like(builder.lower(root.get("email")),
                        "%".concat(filter.getEmail().toLowerCase()).concat("%")));
            }
            if (StringUtils.hasText(filter.getPhoneNumber())) {
                predicates.add(builder.like(builder.lower(root.get("phoneNumber")),
                        "%".concat(filter.getPhoneNumber().toLowerCase()).concat("%")));
            }
            if (Objects.nonNull(filter.getCountryId())) {
                predicates.add(builder.equal(root.join("country", JoinType.LEFT).get("id"), filter.getCountryId()));
            }
            if (Objects.nonNull(filter.getCityId())) {
                predicates.add(builder.equal(root.join("city", JoinType.LEFT).get("id"), filter.getCityId()));
            }
            if (Objects.nonNull(filter.getEducationLevelId())) {
                predicates.add(builder.equal(root.join("educationLevel", JoinType.LEFT).get("id"),
                        filter.getEducationLevelId()));
            }
            if (Objects.nonNull(filter.getProfessionId())) {
                predicates.add(builder.equal(root.join("profession", JoinType.LEFT).get("id"),
                        filter.getProfessionId()));
            }

            return builder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
