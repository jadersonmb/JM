package com.jm.mappers;

import com.jm.dto.ProfessionDTO;
import com.jm.entity.Profession;
import org.mapstruct.BeanMapping;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        builder = @Builder(disableBuilder = true))
public interface ProfessionMapper {

    ProfessionDTO toDTO(Profession entity);

    Profession toEntity(ProfessionDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(ProfessionDTO dto, @MappingTarget Profession entity);
}
