package com.jm.mappers;

import com.jm.dto.PermissionDTO;
import com.jm.security.entity.Permission;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = { ActionMapper.class, ObjectMapper.class })
public interface PermissionMapper {

    PermissionDTO toDto(Permission permission);

    @Mapping(target = "action", ignore = true)
    @Mapping(target = "object", ignore = true)
    Permission toEntity(PermissionDTO dto);
}
