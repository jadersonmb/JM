package com.jm.mappers;

import com.jm.dto.DietMealDTO;
import com.jm.dto.DietMealItemDTO;
import com.jm.dto.DietPlanDTO;
import com.jm.entity.DietMeal;
import com.jm.entity.DietMealItem;
import com.jm.entity.DietPlan;
import com.jm.entity.MeasurementUnits;
import org.springframework.util.StringUtils;
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
    @Mapping(target = "unitDisplayName", expression = "java(mapUnitDisplayName(entity.getUnit()))")
    DietMealItemDTO toMealItemDTO(DietMealItem entity);

    @Mapping(target = "meal", ignore = true)
    @Mapping(target = "foodItem", ignore = true)
    @Mapping(target = "unit", ignore = true)
    DietMealItem toMealItemEntity(DietMealItemDTO dto);

    default String mapUnitDisplayName(MeasurementUnits unit) {
        if (unit == null) {
            return null;
        }
        if (StringUtils.hasText(unit.getDescription())) {
            return unit.getDescription();
        }
        if (StringUtils.hasText(unit.getSymbol())) {
            return unit.getSymbol();
        }
        return unit.getCode();
    }
}

