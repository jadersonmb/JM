package com.jm.repository;

import com.jm.entity.MeasurementUnits;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MeasurementUnitRepository extends JpaRepository<MeasurementUnits, UUID> {

    List<MeasurementUnits> findByLanguageIgnoreCaseOrLanguageIsNullOrderByDescriptionAsc(String language);

    List<MeasurementUnits> findAllByOrderByDescriptionAsc();
}
