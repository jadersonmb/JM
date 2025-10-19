package com.jm.mappers;

import com.jm.dto.UserDTO;
import com.jm.entity.Users;
import org.mapstruct.BeanMapping;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", uses = { CountryMapper.class, RoleMapper.class }, unmappedTargetPolicy = ReportingPolicy.IGNORE,
        builder = @Builder(disableBuilder = true))
public interface UserMapper {

    @Mapping(target = "cityId", source = "city.id")
    @Mapping(target = "cityName", source = "city.name")
    @Mapping(target = "countryId", source = "country.id")
    @Mapping(target = "countryDTO", source = "country")
    @Mapping(target = "educationLevelId", source = "educationLevel.id")
    @Mapping(target = "educationLevelName", source = "educationLevel.name")
    @Mapping(target = "professionId", source = "profession.id")
    @Mapping(target = "professionName", source = "profession.name")
    UserDTO toDTO(Users entity);

    @Mapping(target = "city", ignore = true)
    @Mapping(target = "country", ignore = true)
    @Mapping(target = "educationLevel", ignore = true)
    @Mapping(target = "profession", ignore = true)
    @Mapping(target = "firstAccess", ignore = true)
    @Mapping(target = "imagens", ignore = true)
    @Mapping(target = "anamneses", ignore = true)
    @Mapping(target = "roles", ignore = true)
    Users toEntity(UserDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "city", ignore = true)
    @Mapping(target = "country", ignore = true)
    @Mapping(target = "educationLevel", ignore = true)
    @Mapping(target = "profession", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "firstAccess", ignore = true)
    @Mapping(target = "imagens", ignore = true)
    @Mapping(target = "anamneses", ignore = true)
    @Mapping(target = "roles", ignore = true)
    void updateEntityFromDto(UserDTO dto, @MappingTarget Users entity);
}
