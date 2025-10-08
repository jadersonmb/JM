package com.jm.repository;

import com.jm.entity.FoodCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FoodCategoryRepository extends JpaRepository<FoodCategory, UUID> {

    Optional<FoodCategory> findByNameIgnoreCase(String name);

    List<FoodCategory> findByLanguageIgnoreCaseOrLanguageIsNullOrderByNameAsc(String language);

    List<FoodCategory> findAllByOrderByNameAsc();
}
