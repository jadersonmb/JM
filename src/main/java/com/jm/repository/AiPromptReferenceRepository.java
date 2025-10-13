package com.jm.repository;

import com.jm.entity.AiPromptReference;
import com.jm.enums.AiProvider;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AiPromptReferenceRepository
        extends JpaRepository<AiPromptReference, UUID>, JpaSpecificationExecutor<AiPromptReference> {

    Optional<AiPromptReference> findFirstByCodeIgnoreCaseAndProviderAndModelIgnoreCaseAndOwnerIgnoreCaseAndActiveTrueOrderByUpdatedAtDesc(
            String code, AiProvider provider, String model, String owner);

    Optional<AiPromptReference> findFirstByCodeIgnoreCaseAndProviderAndModelIgnoreCaseAndOwnerIsNullAndActiveTrueOrderByUpdatedAtDesc(
            String code, AiProvider provider, String model);
}
