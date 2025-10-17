package com.jm.mappers;

import com.jm.dto.ExerciseDTO;
import com.jm.entity.Exercise;
import com.jm.entity.Users;
import org.mapstruct.BeanMapping;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.springframework.util.StringUtils;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        builder = @Builder(disableBuilder = true))
public interface ExerciseMapper {

    @Mapping(target = "referenceId", source = "reference.id")
    @Mapping(target = "referenceName", source = "reference.name")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userName", expression = "java(mapUserName(entity.getUser()))")
    ExerciseDTO toDTO(Exercise entity);

    @Mapping(target = "reference", ignore = true)
    @Mapping(target = "user", ignore = true)
    Exercise toEntity(ExerciseDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "reference", ignore = true)
    @Mapping(target = "user", ignore = true)
    void updateEntityFromDto(ExerciseDTO dto, @MappingTarget Exercise entity);

    default String mapUserName(Users user) {
        if (user == null) {
            return null;
        }
        String first = user.getName();
        String last = user.getLastName();
        if (StringUtils.hasText(first) && StringUtils.hasText(last)) {
            return (first + " " + last).trim();
        }
        if (StringUtils.hasText(first)) {
            return first;
        }
        if (StringUtils.hasText(last)) {
            return last;
        }
        return null;
    }
}
