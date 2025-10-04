package com.jm.speciation;

import com.jm.dto.UserDTO;
import com.jm.entity.Users;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UserSpeciation {

    public static Specification<Users> search(UserDTO filter) {
        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (Objects.nonNull(filter.getName()) && !filter.getName().isEmpty()) {
                predicates.add(builder.like(builder.lower(root.<String>get("name")),
                        "%".concat(filter.getName().toLowerCase()).concat("%")));
            }
            if (Objects.nonNull(filter.getPhoneNumber()) && !filter.getPhoneNumber().isEmpty()) {
                predicates.add(builder.equal(builder.lower(root.<String>get("phoneNumber")),
                        filter.getPhoneNumber().toLowerCase()));
            }

            return builder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
