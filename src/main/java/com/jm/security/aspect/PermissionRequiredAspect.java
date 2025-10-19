package com.jm.security.aspect;

import com.jm.execption.JMException;
import com.jm.execption.ProblemType;
import com.jm.security.annotation.PermissionRequired;
import com.jm.security.service.PermissionEvaluator;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Aspect
@Component
@RequiredArgsConstructor
public class PermissionRequiredAspect {

    private final PermissionEvaluator permissionEvaluator;
    private final MessageSource messageSource;

    @Before("@annotation(permissionRequired)")
    public void checkMethodPermission(JoinPoint joinPoint, PermissionRequired permissionRequired) {
        enforce(permissionRequired.value());
    }

    @Before("@within(permissionRequired)")
    public void checkClassPermission(JoinPoint joinPoint, PermissionRequired permissionRequired) {
        enforce(permissionRequired.value());
    }

    private void enforce(String permission) {
        if (permission == null || permission.isBlank()) {
            return;
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean allowed = permissionEvaluator.hasPermission(authentication, permission);
        if (!allowed) {
            throw forbiddenException();
        }
    }

    private JMException forbiddenException() {
        Locale locale = LocaleContextHolder.getLocale();
        String message = messageSource.getMessage("auth.access.denied", null, locale);
        return new JMException(HttpStatus.FORBIDDEN.value(), ProblemType.FORBIDDEN.getUri(),
                ProblemType.FORBIDDEN.getTitle(), message);
    }
}
