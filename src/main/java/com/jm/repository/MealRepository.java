package com.jm.repository;

import com.jm.entity.Meal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MealRepository extends JpaRepository<Meal, UUID>, JpaSpecificationExecutor<Meal> {

    List<Meal> findByLanguageIgnoreCaseOrLanguageIsNullOrderBySortOrderAscNameAsc(String language);

    List<Meal> findAllByOrderBySortOrderAscNameAsc();

    Optional<Meal> findByCodeIgnoreCase(String code);

    Optional<Meal> findByNameIgnoreCase(String name);
}
