package com.jm.mappers;

import com.jm.dto.ExerciseReferenceDTO;
import com.jm.entity.ExerciseReference;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        builder = @Builder(disableBuilder = true))
public interface ExerciseReferenceMapper {

    ExerciseReferenceDTO toDTO(ExerciseReference entity);
}
