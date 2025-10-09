package com.jm.repository;

import com.jm.entity.DietMeal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DietMealRepository extends JpaRepository<DietMeal, UUID> {
}

