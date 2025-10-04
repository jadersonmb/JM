package com.jm.repository;

import com.jm.entity.FoodCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface FoodCategoryRepository extends JpaRepository<FoodCategory, UUID> {
    Optional<FoodCategory> findByNameIgnoreCase(String name);
}
