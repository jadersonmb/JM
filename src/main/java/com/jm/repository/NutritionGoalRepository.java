package com.jm.repository;

import com.jm.entity.NutritionGoal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface NutritionGoalRepository extends JpaRepository<NutritionGoal, UUID>,
        JpaSpecificationExecutor<NutritionGoal> {
}
