package com.jm.repository;

import com.jm.entity.EducationLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EducationLevelRepository extends JpaRepository<EducationLevel, UUID> {

    List<EducationLevel> findByLanguageIgnoreCaseOrLanguageIsNullOrderBySortOrderAscNameAsc(String language);

    List<EducationLevel> findAllByOrderBySortOrderAscNameAsc();
}
