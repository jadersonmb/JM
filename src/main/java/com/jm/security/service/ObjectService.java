package com.jm.security.service;

import com.jm.dto.ObjectDTO;
import com.jm.dto.ObjectRequest;
import com.jm.execption.JMException;
import com.jm.execption.ProblemType;
import com.jm.mappers.ObjectMapper;
import com.jm.security.entity.ObjectEntity;
import com.jm.security.repository.ObjectEntityRepository;
import com.jm.security.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ObjectService {

    private final ObjectEntityRepository objectRepository;
    private final PermissionRepository permissionRepository;
    private final ObjectMapper objectMapper;
    private final MessageSource messageSource;

    public List<ObjectDTO> listAll() {
        return objectRepository.findAll(Sort.by(Sort.Direction.ASC, "name")).stream()
                .map(objectMapper::toDto)
                .toList();
    }

    public ObjectDTO create(ObjectRequest request) {
        String name = normalize(request.getName());
        if (objectRepository.existsByNameIgnoreCase(name)) {
            throw duplicateObject(name);
        }
        ObjectEntity entity = new ObjectEntity();
        entity.setName(name);
        entity.setDescription(normalizeDescription(request.getDescription()));
        return objectMapper.toDto(objectRepository.save(entity));
    }

    public ObjectDTO update(UUID id, ObjectRequest request) {
        ObjectEntity entity = objectRepository.findById(id).orElseThrow(() -> objectNotFound(id));
        String name = normalize(request.getName());
        if (!entity.getName().equalsIgnoreCase(name) && objectRepository.existsByNameIgnoreCase(name)) {
            throw duplicateObject(name);
        }
        entity.setName(name);
        entity.setDescription(normalizeDescription(request.getDescription()));
        return objectMapper.toDto(objectRepository.save(entity));
    }

    public void delete(UUID id) {
        ObjectEntity entity = objectRepository.findById(id).orElseThrow(() -> objectNotFound(id));
        if (permissionRepository.existsByObjectId(entity.getId())) {
            throw objectInUse(entity.getName());
        }
        objectRepository.delete(entity);
    }

    private String normalize(String value) {
        return value == null ? null : value.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeDescription(String value) {
        return value == null ? null : value.trim();
    }

    private JMException objectNotFound(UUID id) {
        Locale locale = LocaleContextHolder.getLocale();
        String message = messageSource.getMessage("objects.not-found", new Object[] { id }, locale);
        return new JMException(HttpStatus.NOT_FOUND.value(), ProblemType.INVALID_DATA.getUri(),
                ProblemType.INVALID_DATA.getTitle(), message);
    }

    private JMException duplicateObject(String name) {
        Locale locale = LocaleContextHolder.getLocale();
        String message = messageSource.getMessage("objects.duplicate", new Object[] { name }, locale);
        return new JMException(HttpStatus.BAD_REQUEST.value(), ProblemType.INVALID_DATA.getUri(),
                ProblemType.INVALID_DATA.getTitle(), message);
    }

    private JMException objectInUse(String name) {
        Locale locale = LocaleContextHolder.getLocale();
        String message = messageSource.getMessage("objects.in-use", new Object[] { name }, locale);
        return new JMException(HttpStatus.CONFLICT.value(), ProblemType.INVALID_DATA.getUri(),
                ProblemType.INVALID_DATA.getTitle(), message);
    }
}
