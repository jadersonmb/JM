package com.jm.security.service;

import com.jm.dto.PermissionDTO;
import com.jm.dto.PermissionRequest;
import com.jm.entity.Role;
import com.jm.execption.JMException;
import com.jm.mappers.PermissionMapper;
import com.jm.security.entity.Action;
import com.jm.security.entity.ObjectEntity;
import com.jm.security.entity.Permission;
import com.jm.security.repository.ActionRepository;
import com.jm.security.repository.ObjectEntityRepository;
import com.jm.security.repository.PermissionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PermissionServiceTest {

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private ActionRepository actionRepository;

    @Mock
    private ObjectEntityRepository objectRepository;

    @Mock
    private PermissionMapper permissionMapper;

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private PermissionService permissionService;

    @BeforeEach
    void setUpLocale() {
        LocaleContextHolder.setLocale(Locale.US);
    }

    @AfterEach
    void clearLocale() {
        LocaleContextHolder.resetLocaleContext();
    }

    @Test
    void createShouldNormalizeCodeAndLinkReferences() {
        UUID actionId = UUID.randomUUID();
        UUID objectId = UUID.randomUUID();
        PermissionRequest request = new PermissionRequest();
        request.setCode("  obj_view  ");
        request.setDescription("  desc  ");
        request.setActionId(actionId);
        request.setObjectId(objectId);

        Action action = new Action(actionId, "VIEW");
        ObjectEntity object = new ObjectEntity(objectId, "OBJECT", "desc");

        when(permissionRepository.findByCodeIgnoreCase("OBJ_VIEW")).thenReturn(Optional.empty());
        when(actionRepository.findById(actionId)).thenReturn(Optional.of(action));
        when(objectRepository.findById(objectId)).thenReturn(Optional.of(object));
        when(permissionRepository.save(any(Permission.class))).thenAnswer(invocation -> {
            Permission permission = invocation.getArgument(0);
            permission.setId(UUID.randomUUID());
            return permission;
        });
        PermissionDTO dto = PermissionDTO.builder().id(UUID.randomUUID()).code("OBJ_VIEW").build();
        when(permissionMapper.toDto(any(Permission.class))).thenReturn(dto);

        PermissionDTO result = permissionService.create(request);

        ArgumentCaptor<Permission> captor = ArgumentCaptor.forClass(Permission.class);
        verify(permissionRepository).save(captor.capture());
        Permission saved = captor.getValue();
        assertThat(saved.getCode()).isEqualTo("OBJ_VIEW");
        assertThat(saved.getDescription()).isEqualTo("desc");
        assertThat(saved.getAction()).isSameAs(action);
        assertThat(saved.getObject()).isSameAs(object);
        assertThat(result).isEqualTo(dto);
    }

    @Test
    void createShouldRejectDuplicateCode() {
        PermissionRequest request = new PermissionRequest();
        request.setCode("duplicate");
        request.setActionId(UUID.randomUUID());
        request.setObjectId(UUID.randomUUID());

        Permission existing = new Permission(UUID.randomUUID(), "DUPLICATE", null, null, null, Set.of());

        when(permissionRepository.findByCodeIgnoreCase("DUPLICATE")).thenReturn(Optional.of(existing));
        when(messageSource.getMessage(eq("permissions.duplicate"), any(Object[].class), any(Locale.class)))
                .thenReturn("duplicate permission");

        JMException exception = assertThrows(JMException.class, () -> permissionService.create(request));
        assertThat(exception.getDetails()).isEqualTo("duplicate permission");
        verify(permissionRepository, never()).save(any(Permission.class));
    }

    @Test
    void updateShouldFailWhenActionMissing() {
        UUID permissionId = UUID.randomUUID();
        UUID actionId = UUID.randomUUID();
        UUID objectId = UUID.randomUUID();
        Permission permission = new Permission(permissionId, "PERMISSION", null, null, null, Set.of());
        PermissionRequest request = new PermissionRequest();
        request.setCode("permission");
        request.setActionId(actionId);
        request.setObjectId(objectId);

        when(permissionRepository.findById(permissionId)).thenReturn(Optional.of(permission));
        when(permissionRepository.findByCodeIgnoreCase("PERMISSION")).thenReturn(Optional.of(permission));
        when(actionRepository.findById(actionId)).thenReturn(Optional.empty());
        when(messageSource.getMessage(eq("actions.not-found"), any(Object[].class), any(Locale.class)))
                .thenReturn("action missing");

        JMException exception = assertThrows(JMException.class, () -> permissionService.update(permissionId, request));
        assertThat(exception.getDetails()).isEqualTo("action missing");
    }

    @Test
    void deleteShouldFailWhenPermissionInUse() {
        UUID id = UUID.randomUUID();
        Permission permission = new Permission(id, "CODE", null, null, null, Set.of(new Role()));

        when(permissionRepository.findById(id)).thenReturn(Optional.of(permission));
        when(messageSource.getMessage(eq("permissions.in-use"), any(Object[].class), any(Locale.class)))
                .thenReturn("permission in use");

        JMException exception = assertThrows(JMException.class, () -> permissionService.delete(id));
        assertThat(exception.getDetails()).isEqualTo("permission in use");
        verify(permissionRepository, never()).delete(any(Permission.class));
    }

    @Test
    void findByIdsShouldValidateMissingPermissions() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        when(permissionRepository.findAllById(anyCollection())).thenReturn(List.of(new Permission(id1, "CODE", null, null, null, Set.of())));
        when(messageSource.getMessage(anyString(), any(), any())).thenReturn("invalid selection");

        JMException exception = assertThrows(JMException.class,
                () -> permissionService.findByIds(Set.of(id1, id2)));
        assertThat(exception.getDetails()).isEqualTo("invalid selection");
    }

    @Test
    void findByCodesShouldValidateMissingPermission() {
        when(messageSource.getMessage(anyString(), any(), any())).thenReturn("invalid selection");

        JMException exception = assertThrows(JMException.class,
                () -> permissionService.findByCodes(List.of("CODE")));
        assertThat(exception.getDetails()).isEqualTo("invalid selection");
    }
}
