package com.jm.security.repository;

import com.jm.security.entity.Action;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ActionRepository extends JpaRepository<Action, UUID> {

    Optional<Action> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);
}
