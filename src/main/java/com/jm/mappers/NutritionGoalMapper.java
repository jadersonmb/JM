package com.jm.mappers;

import com.jm.dto.NutritionGoalDTO;
import com.jm.entity.MeasurementUnits;
import com.jm.entity.NutritionGoal;
import com.jm.entity.Users;
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
public interface NutritionGoalMapper {

    @Mapping(target = "unitId", source = "unit.id")
    @Mapping(target = "unitName", source = "unit", qualifiedByName = "mapUnitName")
    @Mapping(target = "unitSymbol", source = "unit.symbol")
    @Mapping(target = "createdByUserId", source = "createdBy.id")
    @Mapping(target = "createdByUserName", expression = "java(mapUserName(entity.getCreatedBy()))")
    @Mapping(target = "templateId", source = "template.id")
    @Mapping(target = "templateName", source = "template.name")
    NutritionGoalDTO toDTO(NutritionGoal entity);

    @Mapping(target = "unit", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "template", ignore = true)
    NutritionGoal toEntity(NutritionGoalDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "unit", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "template", ignore = true)
    void updateEntityFromDto(NutritionGoalDTO dto, @MappingTarget NutritionGoal entity);

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

    default String mapUserName(Users user) {
        if (user == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        if (user.getName() != null) {
            builder.append(user.getName());
        }
        if (user.getLastName() != null && !user.getLastName().isBlank()) {
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(user.getLastName());
        }
        if (builder.isEmpty() && user.getEmail() != null) {
            builder.append(user.getEmail());
        }
        return builder.isEmpty() ? null : builder.toString();
    }
}
