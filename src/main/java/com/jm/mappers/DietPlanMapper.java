package com.jm.mappers;

import com.jm.dto.DietMealDTO;
import com.jm.dto.DietMealItemDTO;
import com.jm.dto.DietPlanDTO;
import com.jm.dto.FoodItemDTO;
import com.jm.dto.UnitOfMeasureDTO;
import com.jm.entity.DietMeal;
import com.jm.entity.DietMealItem;
import com.jm.entity.DietPlan;
import com.jm.entity.FoodItem;
import com.jm.entity.UnitOfMeasure;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        builder = @Builder(disableBuilder = true))
public interface DietPlanMapper {

    DietPlanDTO toDTO(DietPlan entity);

    DietPlan toEntity(DietPlanDTO dto);

    @Mapping(target = "dietPlanId", source = "dietPlan.id")
    DietMealDTO toMealDTO(DietMeal entity);

    @Mapping(target = "dietPlan", ignore = true)
    DietMeal toMealEntity(DietMealDTO dto);

    @Mapping(target = "dietMealId", source = "meal.id")
    @Mapping(target = "foodItemId", source = "foodItem.id")
    @Mapping(target = "foodItemName", source = "foodItem.name")
    @Mapping(target = "unitId", source = "unit.id")
    @Mapping(target = "unitDisplayName", source = "unit.displayName")
    DietMealItemDTO toMealItemDTO(DietMealItem entity);

    @Mapping(target = "meal", ignore = true)
    @Mapping(target = "foodItem", ignore = true)
    @Mapping(target = "unit", ignore = true)
    DietMealItem toMealItemEntity(DietMealItemDTO dto);

    FoodItemDTO toFoodItemDTO(FoodItem entity);

    FoodItem toFoodItemEntity(FoodItemDTO dto);

    UnitOfMeasureDTO toUnitOfMeasureDTO(UnitOfMeasure entity);

    UnitOfMeasure toUnitOfMeasureEntity(UnitOfMeasureDTO dto);
}

