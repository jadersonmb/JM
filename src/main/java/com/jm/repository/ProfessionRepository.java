package com.jm.repository;

import com.jm.entity.Profession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProfessionRepository extends JpaRepository<Profession, UUID>, JpaSpecificationExecutor<Profession> {

    List<Profession> findByLanguageIgnoreCaseOrLanguageIsNullOrderByNameAsc(String language);

    List<Profession> findAllByOrderByNameAsc();
}
