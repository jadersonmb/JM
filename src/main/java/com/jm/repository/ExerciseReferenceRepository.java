package com.jm.repository;

import com.jm.entity.ExerciseReference;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ExerciseReferenceRepository
        extends JpaRepository<ExerciseReference, UUID>, JpaSpecificationExecutor<ExerciseReference> {

    List<ExerciseReference> findByLanguageIgnoreCase(String language, Sort sort);
}
