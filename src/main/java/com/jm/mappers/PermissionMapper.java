package com.jm.mappers;

import com.jm.dto.PermissionDTO;
import com.jm.security.entity.Permission;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PermissionMapper {

    PermissionDTO toDto(Permission permission);

    Permission toEntity(PermissionDTO dto);
}
