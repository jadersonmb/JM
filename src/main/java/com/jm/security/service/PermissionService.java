package com.jm.security.service;

import com.jm.execption.JMException;
import com.jm.execption.ProblemType;
import com.jm.security.entity.Permission;
import com.jm.security.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
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
    private final MessageSource messageSource;

    public List<Permission> findAll() {
        return permissionRepository.findAll();
    }

    public Set<Permission> findByIds(Collection<UUID> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Set.of();
        }
        List<Permission> permissions = permissionRepository.findAllById(ids);
        if (permissions.size() != ids.size()) {
            throw invalidPermission();
        }
        return permissions.stream().collect(Collectors.toSet());
    }

    public Set<Permission> findByCodes(Collection<String> codes) {
        if (CollectionUtils.isEmpty(codes)) {
            return Set.of();
        }
        Set<Permission> permissions = codes.stream()
                .map(code -> permissionRepository.findByCode(code)
                        .orElseThrow(this::invalidPermission))
                .collect(Collectors.toSet());
        return permissions;
    }

    private JMException invalidPermission() {
        Locale locale = LocaleContextHolder.getLocale();
        String message = messageSource.getMessage("auth.access.denied", null, locale);
        return new JMException(HttpStatus.BAD_REQUEST.value(), ProblemType.INVALID_DATA.getUri(),
                ProblemType.INVALID_DATA.getTitle(), message);
    }
}
