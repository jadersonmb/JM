package com.jm.mappers;

import com.jm.dto.ActionDTO;
import com.jm.security.entity.Action;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ActionMapper {

    ActionDTO toDto(Action action);
}
