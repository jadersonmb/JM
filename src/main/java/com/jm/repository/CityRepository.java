package com.jm.repository;

import com.jm.entity.City;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CityRepository extends JpaRepository<City, UUID>, JpaSpecificationExecutor<City> {

    List<City> findByCountryIdOrderByNameAsc(UUID countryId);

    List<City> findByCountryIdAndLanguageIgnoreCaseOrderByNameAsc(UUID countryId, String language);

    List<City> findByCountryIdAndLanguageIsNullOrderByNameAsc(UUID countryId);
}
