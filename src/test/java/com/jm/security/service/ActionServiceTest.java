package com.jm.security.service;

import com.jm.dto.ActionDTO;
import com.jm.dto.ActionRequest;
import com.jm.execption.JMException;
import com.jm.mappers.ActionMapper;
import com.jm.security.entity.Action;
import com.jm.security.repository.ActionRepository;
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

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActionServiceTest {

    @Mock
    private ActionRepository actionRepository;

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private ActionMapper actionMapper;

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private ActionService actionService;

    @BeforeEach
    void setUpLocale() {
        LocaleContextHolder.setLocale(Locale.US);
    }

    @AfterEach
    void clearLocale() {
        LocaleContextHolder.resetLocaleContext();
    }

    @Test
    void createShouldNormalizeNameAndPersist() {
        ActionRequest request = new ActionRequest();
        request.setName("  inserir  ");

        when(actionRepository.existsByNameIgnoreCase("INSERIR")).thenReturn(false);
        when(actionRepository.save(any(Action.class))).thenAnswer(invocation -> {
            Action action = invocation.getArgument(0);
            action.setId(UUID.randomUUID());
            return action;
        });
        ActionDTO expectedDto = ActionDTO.builder().id(UUID.randomUUID()).name("INSERIR").build();
        when(actionMapper.toDto(any(Action.class))).thenReturn(expectedDto);

        ActionDTO result = actionService.create(request);

        ArgumentCaptor<Action> captor = ArgumentCaptor.forClass(Action.class);
        verify(actionRepository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("INSERIR");
        assertThat(result).isEqualTo(expectedDto);
    }

    @Test
    void createShouldRejectDuplicateName() {
        ActionRequest request = new ActionRequest();
        request.setName("Inserir");

        when(actionRepository.existsByNameIgnoreCase("INSERIR")).thenReturn(true);
        when(messageSource.getMessage(eq("actions.duplicate"), any(Object[].class), any(Locale.class)))
                .thenReturn("duplicate action");

        JMException exception = assertThrows(JMException.class, () -> actionService.create(request));
        assertThat(exception.getDetails()).isEqualTo("duplicate action");
        verify(actionRepository, never()).save(any(Action.class));
    }

    @Test
    void updateShouldPreventRenamingToExistingAction() {
        UUID id = UUID.randomUUID();
        Action existing = new Action(id, "EXISTING");
        ActionRequest request = new ActionRequest();
        request.setName("New Name");

        when(actionRepository.findById(id)).thenReturn(Optional.of(existing));
        when(actionRepository.existsByNameIgnoreCase("NEW NAME")).thenReturn(true);
        when(messageSource.getMessage(eq("actions.duplicate"), any(Object[].class), any(Locale.class)))
                .thenReturn("duplicate action");

        JMException exception = assertThrows(JMException.class, () -> actionService.update(id, request));
        assertThat(exception.getDetails()).isEqualTo("duplicate action");
        verify(actionRepository, never()).save(any(Action.class));
    }

    @Test
    void deleteShouldFailWhenActionInUse() {
        UUID id = UUID.randomUUID();
        Action existing = new Action(id, "IN USE");

        when(actionRepository.findById(id)).thenReturn(Optional.of(existing));
        when(permissionRepository.existsByActionId(id)).thenReturn(true);
        when(messageSource.getMessage(eq("actions.in-use"), any(Object[].class), any(Locale.class)))
                .thenReturn("action in use");

        JMException exception = assertThrows(JMException.class, () -> actionService.delete(id));
        assertThat(exception.getDetails()).isEqualTo("action in use");
        verify(actionRepository, never()).delete(any(Action.class));
    }
}
