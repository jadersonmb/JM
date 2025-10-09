package com.jm.repository;

import com.jm.entity.UnitOfMeasure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UnitOfMeasureRepository extends JpaRepository<UnitOfMeasure, UUID> {

    List<UnitOfMeasure> findByActiveTrueOrderByDisplayNameAsc();
}

