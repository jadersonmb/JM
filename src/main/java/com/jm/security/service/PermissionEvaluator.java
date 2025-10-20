package com.jm.security.service;

import com.jm.entity.Users;
import com.jm.execption.JMException;
import com.jm.execption.ProblemType;
import com.jm.repository.UserRepository;
import com.jm.security.entity.Permission;
import com.jm.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.UUID;

@Component("permissionEvaluator")
@RequiredArgsConstructor
public class PermissionEvaluator {

    private final UserRepository userRepository;
    private final MessageSource messageSource;

    public boolean hasPermission(Authentication authentication, String permission) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw accessDenied();
        }

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        if (authorities != null) {
            for (GrantedAuthority authority : authorities) {
                if (authority == null) {
                    continue;
                }
                String value = authority.getAuthority();
                if (StringUtils.hasText(value) && value.equalsIgnoreCase(permission)) {
                    return true;
                }
            }
        }

        String username = extractUsername(authentication);
        Users user = resolveCurrentUser(username);

        Set<String> permissions = user.getRoles().stream()
                .filter(role -> role.getPermissions() != null)
                .flatMap(role -> role.getPermissions().stream())
                .map(Permission::getCode)
                .filter(StringUtils::hasText)
                .map(code -> code.toUpperCase(Locale.ROOT))
                .collect(Collectors.toSet());

        return permissions.contains(permission.toUpperCase(Locale.ROOT));
    }

    private String extractUsername(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }
        return authentication.getName();
    }

    private Users resolveCurrentUser(String username) {
        UUID userId = SecurityUtils.getCurrentUserId().orElse(null);
        if (userId != null) {
            return userRepository.findById(userId).orElseThrow(this::accessDenied);
        }

        if (!StringUtils.hasText(username)) {
            throw accessDenied();
        }

        return userRepository.findByEmail(username)
                .orElseThrow(this::accessDenied);
    }

    private JMException accessDenied() {
        Locale locale = LocaleContextHolder.getLocale();
        String message = messageSource.getMessage("auth.access.denied", null, locale);
        return new JMException(HttpStatus.FORBIDDEN.value(), ProblemType.FORBIDDEN.getUri(),
                ProblemType.FORBIDDEN.getTitle(), message);
    }
}
