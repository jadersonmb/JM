package com.jm.repository;

import com.jm.entity.NutritionGoalTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface NutritionGoalTemplateRepository extends JpaRepository<NutritionGoalTemplate, UUID>,
        JpaSpecificationExecutor<NutritionGoalTemplate> {
}
