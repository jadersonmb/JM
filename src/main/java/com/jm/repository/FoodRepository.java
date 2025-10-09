package com.jm.repository;

import com.jm.entity.Food;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FoodRepository extends JpaRepository<Food, UUID> {

    List<Food> findByLanguageIgnoreCaseOrderByNameAsc(String language);

    List<Food> findByLanguageIsNullOrderByNameAsc();

    List<Food> findByFoodCategoryIdAndLanguageIgnoreCaseOrderByNameAsc(UUID categoryId, String language);

    List<Food> findByFoodCategoryIdAndLanguageIsNullOrderByNameAsc(UUID categoryId);

    List<Food> findAllByOrderByNameAsc();

    List<Food> findByIsActiveTrueOrderByNameAsc();
}
