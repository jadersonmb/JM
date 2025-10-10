package com.jm.mappers;

import com.jm.dto.NutritionGoalTemplateDTO;
import com.jm.entity.MeasurementUnits;
import com.jm.entity.NutritionGoalTemplate;
import org.mapstruct.BeanMapping;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        builder = @Builder(disableBuilder = true))
public interface NutritionGoalTemplateMapper {

    @Mapping(target = "unitId", source = "unit.id")
    @Mapping(target = "unitName", source = "unit", qualifiedByName = "mapUnitName")
    @Mapping(target = "unitSymbol", source = "unit.symbol")
    NutritionGoalTemplateDTO toDTO(NutritionGoalTemplate entity);

    @Mapping(target = "unit", ignore = true)
    NutritionGoalTemplate toEntity(NutritionGoalTemplateDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "unit", ignore = true)
    void updateEntityFromDto(NutritionGoalTemplateDTO dto, @MappingTarget NutritionGoalTemplate entity);

    @Named("mapUnitName")
    default String mapUnitName(MeasurementUnits unit) {
        if (unit == null) {
            return null;
        }
        if (unit.getDescription() != null && !unit.getDescription().isBlank()) {
            return unit.getDescription();
        }
        return unit.getSymbol();
    }
}
