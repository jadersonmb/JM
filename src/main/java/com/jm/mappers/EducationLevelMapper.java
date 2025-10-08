package com.jm.mappers;

import com.jm.dto.EducationLevelDTO;
import com.jm.entity.EducationLevel;
import org.mapstruct.BeanMapping;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        builder = @Builder(disableBuilder = true))
public interface EducationLevelMapper {

    EducationLevelDTO toDTO(EducationLevel entity);

    EducationLevel toEntity(EducationLevelDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(EducationLevelDTO dto, @MappingTarget EducationLevel entity);
}
