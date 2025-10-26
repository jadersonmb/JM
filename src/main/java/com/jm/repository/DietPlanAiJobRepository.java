package com.jm.repository;

import com.jm.entity.DietPlanAiJob;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DietPlanAiJobRepository extends JpaRepository<DietPlanAiJob, UUID> {
}
