package com.jm.mappers;

import com.jm.dto.CountryDTO;
import com.jm.entity.Country;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CountryMapper {

    CountryDTO toDTO(Country entity);

    Country toEntity(CountryDTO dto);

}
