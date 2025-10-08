package com.jm.repository;

import com.jm.entity.BiochemicalExam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BiochemicalExamRepository extends JpaRepository<BiochemicalExam, UUID> {

    List<BiochemicalExam> findByLanguageIgnoreCaseOrLanguageIsNullOrderByNameAsc(String language);

    List<BiochemicalExam> findAllByOrderByNameAsc();
}
