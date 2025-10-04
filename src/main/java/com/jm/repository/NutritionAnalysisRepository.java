package com.jm.repository;

import com.jm.entity.NutritionAnalysis;
import com.jm.entity.WhatsAppMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NutritionAnalysisRepository extends JpaRepository<NutritionAnalysis, UUID> {
    Optional<NutritionAnalysis> findByMessage(WhatsAppMessage message);
    List<NutritionAnalysis> findTop20ByOrderByCreatedAtDesc();
    List<NutritionAnalysis> findByCreatedAtAfter(OffsetDateTime createdAt);
}
