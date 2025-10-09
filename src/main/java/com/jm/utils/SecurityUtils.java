package com.jm.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static Optional<UUID> getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return Optional.empty();
        }

        if (authentication instanceof JwtAuthenticationToken jwtAuthenticationToken) {
            Jwt token = jwtAuthenticationToken.getToken();
            String subject = token.getSubject();
            Optional<UUID> fromSubject = parseUuid(subject);
            if (fromSubject.isPresent()) {
                return fromSubject;
            }
            String userIdClaim = token.getClaimAsString("userID");
            Optional<UUID> fromClaim = parseUuid(userIdClaim);
            if (fromClaim.isPresent()) {
                return fromClaim;
            }
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof Jwt jwt) {
            Optional<UUID> fromSubject = parseUuid(jwt.getSubject());
            if (fromSubject.isPresent()) {
                return fromSubject;
            }
            return parseUuid(jwt.getClaimAsString("userID"));
        }

        return parseUuid(authentication.getName());
    }

    public static boolean hasRole(String role) {
        if (!StringUtils.hasText(role)) {
            return false;
        }
        String expected = role.toUpperCase(Locale.ROOT);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        if (authorities != null) {
            for (GrantedAuthority authority : authorities) {
                String value = authority.getAuthority();
                if (!StringUtils.hasText(value)) {
                    continue;
                }
                String normalized = value.replace("ROLE_", "").toUpperCase(Locale.ROOT);
                if (normalized.equals(expected)) {
                    return true;
                }
            }
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof Jwt jwt) {
            String singleRole = jwt.getClaimAsString("role");
            if (StringUtils.hasText(singleRole) && singleRole.toUpperCase(Locale.ROOT).equals(expected)) {
                return true;
            }

            Object authoritiesClaim = jwt.getClaims().get("authorities");
            if (authoritiesClaim instanceof Collection<?> collection) {
                for (Object entry : collection) {
                    if (entry != null && entry.toString().replace("ROLE_", "").equalsIgnoreCase(expected)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private static Optional<UUID> parseUuid(String value) {
        if (!StringUtils.hasText(value)) {
            return Optional.empty();
        }
        try {
            return Optional.of(UUID.fromString(value));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }
}

