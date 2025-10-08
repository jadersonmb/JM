package com.jm.repository;

import com.jm.entity.Meal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MealRepository extends JpaRepository<Meal, UUID> {

    List<Meal> findByLanguageIgnoreCaseOrLanguageIsNullOrderBySortOrderAscNameAsc(String language);

    List<Meal> findAllByOrderBySortOrderAscNameAsc();
}
