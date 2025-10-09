package com.jm.repository;

import com.jm.entity.DietPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DietPlanRepository extends JpaRepository<DietPlan, UUID>, JpaSpecificationExecutor<DietPlan> {
}

