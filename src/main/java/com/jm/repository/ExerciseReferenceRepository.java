package com.jm.repository;

import com.jm.entity.ExerciseReference;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ExerciseReferenceRepository extends JpaRepository<ExerciseReference, UUID> {

    List<ExerciseReference> findByLanguageIgnoreCase(String language, Sort sort);
}
