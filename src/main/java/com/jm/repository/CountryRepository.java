package com.jm.repository;

import com.jm.entity.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CountryRepository extends JpaRepository<Country, UUID>, JpaSpecificationExecutor<Country> {

    List<Country> findByLanguageIgnoreCaseOrLanguageIsNullOrderByNameAsc(String language);

    List<Country> findAllByOrderByNameAsc();
}
