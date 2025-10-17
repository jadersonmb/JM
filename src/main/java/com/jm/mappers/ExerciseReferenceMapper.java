package com.jm.mappers;

import com.jm.dto.ExerciseReferenceDTO;
import com.jm.entity.ExerciseReference;
import org.mapstruct.BeanMapping;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        builder = @Builder(disableBuilder = true))
public interface ExerciseReferenceMapper {

    ExerciseReferenceDTO toDTO(ExerciseReference entity);

    ExerciseReference toEntity(ExerciseReferenceDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(ExerciseReferenceDTO dto, @MappingTarget ExerciseReference entity);
}
