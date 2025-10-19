package com.jm.security.repository;

import com.jm.security.entity.ObjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ObjectEntityRepository extends JpaRepository<ObjectEntity, UUID> {

    Optional<ObjectEntity> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);
}
