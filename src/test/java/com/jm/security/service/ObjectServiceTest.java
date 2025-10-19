package com.jm.security.service;

import com.jm.dto.ObjectDTO;
import com.jm.dto.ObjectRequest;
import com.jm.execption.JMException;
import com.jm.mappers.ObjectMapper;
import com.jm.security.entity.ObjectEntity;
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
class ObjectServiceTest {

    @Mock
    private ObjectEntityRepository objectRepository;

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private ObjectService objectService;

    @BeforeEach
    void setUpLocale() {
        LocaleContextHolder.setLocale(Locale.US);
    }

    @AfterEach
    void clearLocale() {
        LocaleContextHolder.resetLocaleContext();
    }

    @Test
    void createShouldNormalizeNameAndDescription() {
        ObjectRequest request = new ObjectRequest();
        request.setName("  Dashboard  ");
        request.setDescription("  Some desc  ");

        when(objectRepository.existsByNameIgnoreCase("DASHBOARD")).thenReturn(false);
        when(objectRepository.save(any(ObjectEntity.class))).thenAnswer(invocation -> {
            ObjectEntity entity = invocation.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });
        ObjectDTO dto = ObjectDTO.builder().id(UUID.randomUUID()).name("DASHBOARD").description("Some desc").build();
        when(objectMapper.toDto(any(ObjectEntity.class))).thenReturn(dto);

        ObjectDTO result = objectService.create(request);

        ArgumentCaptor<ObjectEntity> captor = ArgumentCaptor.forClass(ObjectEntity.class);
        verify(objectRepository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("DASHBOARD");
        assertThat(captor.getValue().getDescription()).isEqualTo("Some desc");
        assertThat(result).isEqualTo(dto);
    }

    @Test
    void updateShouldPreventDuplicateName() {
        UUID id = UUID.randomUUID();
        ObjectEntity existing = new ObjectEntity(id, "CURRENT", "Old desc");
        ObjectRequest request = new ObjectRequest();
        request.setName("New");

        when(objectRepository.findById(id)).thenReturn(Optional.of(existing));
        when(objectRepository.existsByNameIgnoreCase("NEW")).thenReturn(true);
        when(messageSource.getMessage(eq("objects.duplicate"), any(Object[].class), any(Locale.class)))
                .thenReturn("duplicate object");

        JMException exception = assertThrows(JMException.class, () -> objectService.update(id, request));
        assertThat(exception.getDetails()).isEqualTo("duplicate object");
        verify(objectRepository, never()).save(any(ObjectEntity.class));
    }

    @Test
    void deleteShouldFailWhenObjectInUse() {
        UUID id = UUID.randomUUID();
        ObjectEntity existing = new ObjectEntity(id, "OBJECT", "desc");

        when(objectRepository.findById(id)).thenReturn(Optional.of(existing));
        when(permissionRepository.existsByObjectId(id)).thenReturn(true);
        when(messageSource.getMessage(eq("objects.in-use"), any(Object[].class), any(Locale.class)))
                .thenReturn("object in use");

        JMException exception = assertThrows(JMException.class, () -> objectService.delete(id));
        assertThat(exception.getDetails()).isEqualTo("object in use");
        verify(objectRepository, never()).delete(any(ObjectEntity.class));
    }
}
