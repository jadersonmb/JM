package com.jm.speciation;

import com.jm.dto.PersonDTO;
import com.jm.entity.Person;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

public class PersonSpecification {

    public static Specification<Person> search(PersonDTO filter) {
        return (root, query, cb) -> {
            Predicate predicate = cb.conjunction();

            if (filter.getFirstName() != null) {
                predicate = cb.and(predicate, cb.like(cb.lower(root.get("firstName")),
                        "%" + filter.getFirstName().toLowerCase() + "%"));
            }
            if (filter.getLastName() != null) {
                predicate = cb.and(predicate, cb.like(cb.lower(root.get("lastName")),
                        "%" + filter.getLastName().toLowerCase() + "%"));
            }
            if (filter.getEmail() != null) {
                predicate = cb.and(predicate, cb.equal(cb.lower(root.get("email")),
                        filter.getEmail().toLowerCase()));
            }
            return predicate;
        };
    }
}

