package com.jm.services;

import com.jm.dto.PermissionDTO;
import com.jm.dto.RoleDTO;
import com.jm.entity.Role;
import com.jm.execption.JMException;
import com.jm.execption.ProblemType;
import com.jm.mappers.RoleMapper;
import com.jm.repository.RoleRepository;
import com.jm.security.entity.Permission;
import com.jm.security.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;
    private final MessageSource messageSource;
    private final PermissionService permissionService;

    public List<RoleDTO> findAll() {
        return roleRepository.findAll().stream()
                .map(roleMapper::toDto)
                .collect(Collectors.toList());
    }

    public RoleDTO create(RoleDTO dto) {
        Role role = roleMapper.toEntity(dto);
        role.setPermissions(resolvePermissions(dto));
        Role saved = roleRepository.save(role);
        return roleMapper.toDto(saved);
    }

    public RoleDTO update(UUID id, RoleDTO dto) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> roleNotFound(id));
        role.setName(dto.getName());
        role.setDescription(dto.getDescription());
        role.setPermissions(resolvePermissions(dto));
        Role saved = roleRepository.save(role);
        return roleMapper.toDto(saved);
    }

    public void delete(UUID id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> roleNotFound(id));
        roleRepository.delete(role);
    }

    private JMException roleNotFound(UUID id) {
        return new JMException(HttpStatus.NOT_FOUND.value(), ProblemType.INVALID_DATA.getUri(),
                ProblemType.INVALID_DATA.getTitle(),
                messageSource.getMessage("roles.not-found", new Object[] { id }, LocaleContextHolder.getLocale()));
    }

    private Set<Permission> resolvePermissions(RoleDTO dto) {
        if (dto == null || dto.getPermissions() == null) {
            return new HashSet<>();
        }
        Set<UUID> permissionIds = dto.getPermissions().stream()
                .map(PermissionDTO::getId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());
        return new HashSet<>(permissionService.findByIds(permissionIds));
    }
}
