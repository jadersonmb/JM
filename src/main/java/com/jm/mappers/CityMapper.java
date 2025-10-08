package com.jm.mappers;

import com.jm.dto.CityDTO;
import com.jm.entity.City;
import org.mapstruct.BeanMapping;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", uses = { CountryMapper.class },
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        builder = @Builder(disableBuilder = true))
public interface CityMapper {

    @Mapping(target = "countryId", source = "country.id")
    @Mapping(target = "countryName", source = "country.name")
    @Mapping(target = "capital", source = "isCapital")
    @Mapping(target = "active", source = "isActive")
    CityDTO toDTO(City entity);

    @Mapping(target = "country", ignore = true)
    @Mapping(target = "isCapital", source = "capital")
    @Mapping(target = "isActive", source = "active")
    City toEntity(CityDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "country", ignore = true)
    @Mapping(target = "isCapital", source = "capital")
    @Mapping(target = "isActive", source = "active")
    void updateEntityFromDto(CityDTO dto, @MappingTarget City entity);
}
