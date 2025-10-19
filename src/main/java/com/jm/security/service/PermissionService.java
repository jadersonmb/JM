package com.jm.security.service;

import com.jm.dto.PermissionDTO;
import com.jm.dto.PermissionRequest;
import com.jm.execption.JMException;
import com.jm.execption.ProblemType;
import com.jm.mappers.PermissionMapper;
import com.jm.security.entity.Action;
import com.jm.security.entity.ObjectEntity;
import com.jm.security.entity.Permission;
import com.jm.security.repository.ActionRepository;
import com.jm.security.repository.ObjectEntityRepository;
import com.jm.security.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final PermissionRepository permissionRepository;
    private final ActionRepository actionRepository;
    private final ObjectEntityRepository objectRepository;
    private final PermissionMapper permissionMapper;
    private final MessageSource messageSource;

    public List<PermissionDTO> listAll() {
        return permissionRepository.findAll(Sort.by(Sort.Direction.ASC, "code")).stream()
                .map(permissionMapper::toDto)
                .toList();
    }

    public PermissionDTO create(PermissionRequest request) {
        Permission permission = new Permission();
        applyRequest(permission, request);
        return permissionMapper.toDto(permissionRepository.save(permission));
    }

    public PermissionDTO update(UUID id, PermissionRequest request) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> permissionNotFound(id));
        applyRequest(permission, request);
        return permissionMapper.toDto(permissionRepository.save(permission));
    }

    public void delete(UUID id) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> permissionNotFound(id));
        if (permission.getRoles() != null && !permission.getRoles().isEmpty()) {
            throw permissionInUse(permission.getCode());
        }
        permissionRepository.delete(permission);
    }

    public Set<Permission> findByIds(Collection<UUID> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Set.of();
        }
        List<Permission> permissions = permissionRepository.findAllById(ids);
        if (permissions.size() != ids.size()) {
            throw invalidPermissionSelection();
        }
        return permissions.stream().collect(Collectors.toSet());
    }

    public Set<Permission> findByCodes(Collection<String> codes) {
        if (CollectionUtils.isEmpty(codes)) {
            return Set.of();
        }
        return codes.stream()
                .map(code -> permissionRepository.findByCodeIgnoreCase(normalizeCode(code))
                        .orElseThrow(this::invalidPermissionSelection))
                .collect(Collectors.toSet());
    }

    private void applyRequest(Permission permission, PermissionRequest request) {
        String code = normalizeCode(request.getCode());
        permissionRepository.findByCodeIgnoreCase(code)
                .filter(existing -> !existing.getId().equals(permission.getId()))
                .ifPresent(existing -> { throw duplicatePermission(code); });

        permission.setCode(code);
        permission.setDescription(normalizeDescription(request.getDescription()));
        permission.setAction(resolveAction(request.getActionId()));
        permission.setObject(resolveObject(request.getObjectId()));
    }

    private Action resolveAction(UUID id) {
        return actionRepository.findById(id)
                .orElseThrow(() -> actionNotFound(id));
    }

    private ObjectEntity resolveObject(UUID id) {
        return objectRepository.findById(id)
                .orElseThrow(() -> objectNotFound(id));
    }

    private String normalizeCode(String code) {
        return code == null ? null : code.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeDescription(String value) {
        return value == null ? null : value.trim();
    }

    private JMException invalidPermissionSelection() {
        Locale locale = LocaleContextHolder.getLocale();
        String message = messageSource.getMessage("permissions.invalid-selection", null, locale);
        return new JMException(HttpStatus.BAD_REQUEST.value(), ProblemType.INVALID_DATA.getUri(),
                ProblemType.INVALID_DATA.getTitle(), message);
    }

    private JMException actionNotFound(UUID id) {
        Locale locale = LocaleContextHolder.getLocale();
        String message = messageSource.getMessage("actions.not-found", new Object[] { id }, locale);
        return new JMException(HttpStatus.NOT_FOUND.value(), ProblemType.INVALID_DATA.getUri(),
                ProblemType.INVALID_DATA.getTitle(), message);
    }

    private JMException objectNotFound(UUID id) {
        Locale locale = LocaleContextHolder.getLocale();
        String message = messageSource.getMessage("objects.not-found", new Object[] { id }, locale);
        return new JMException(HttpStatus.NOT_FOUND.value(), ProblemType.INVALID_DATA.getUri(),
                ProblemType.INVALID_DATA.getTitle(), message);
    }

    private JMException permissionNotFound(UUID id) {
        Locale locale = LocaleContextHolder.getLocale();
        String message = messageSource.getMessage("permissions.not-found", new Object[] { id }, locale);
        return new JMException(HttpStatus.NOT_FOUND.value(), ProblemType.INVALID_DATA.getUri(),
                ProblemType.INVALID_DATA.getTitle(), message);
    }

    private JMException duplicatePermission(String code) {
        Locale locale = LocaleContextHolder.getLocale();
        String message = messageSource.getMessage("permissions.duplicate", new Object[] { code }, locale);
        return new JMException(HttpStatus.BAD_REQUEST.value(), ProblemType.INVALID_DATA.getUri(),
                ProblemType.INVALID_DATA.getTitle(), message);
    }

    private JMException permissionInUse(String code) {
        Locale locale = LocaleContextHolder.getLocale();
        String message = messageSource.getMessage("permissions.in-use", new Object[] { code }, locale);
        return new JMException(HttpStatus.CONFLICT.value(), ProblemType.INVALID_DATA.getUri(),
                ProblemType.INVALID_DATA.getTitle(), message);
    }
}
