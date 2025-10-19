package com.jm.mappers;

import com.jm.dto.RoleDTO;
import com.jm.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RoleMapper {

    RoleDTO toDto(Role role);

    Role toEntity(RoleDTO dto);
}
