package com.jm.repository;

import com.jm.entity.DietMealItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DietMealItemRepository extends JpaRepository<DietMealItem, UUID> {
}

