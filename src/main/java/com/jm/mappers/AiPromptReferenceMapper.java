package com.jm.mappers;

import com.jm.dto.AiPromptReferenceDTO;
import com.jm.entity.AiPromptReference;
import org.mapstruct.BeanMapping;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        builder = @Builder(disableBuilder = true))
public interface AiPromptReferenceMapper {

    AiPromptReferenceDTO toDTO(AiPromptReference entity);

    AiPromptReference toEntity(AiPromptReferenceDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(AiPromptReferenceDTO dto, @MappingTarget AiPromptReference entity);
}
