package com.jm.repository;

import com.jm.entity.Pathology;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PathologyRepository extends JpaRepository<Pathology, UUID> {

    List<Pathology> findByLanguageIgnoreCaseOrLanguageIsNullOrderByNameAsc(String language);

    List<Pathology> findAllByOrderByNameAsc();
}
