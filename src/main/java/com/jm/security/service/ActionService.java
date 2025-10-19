package com.jm.security.service;

import com.jm.dto.ActionDTO;
import com.jm.dto.ActionRequest;
import com.jm.execption.JMException;
import com.jm.execption.ProblemType;
import com.jm.mappers.ActionMapper;
import com.jm.security.entity.Action;
import com.jm.security.repository.ActionRepository;
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
public class ActionService {

    private final ActionRepository actionRepository;
    private final PermissionRepository permissionRepository;
    private final ActionMapper actionMapper;
    private final MessageSource messageSource;

    public List<ActionDTO> listAll() {
        return actionRepository.findAll(Sort.by(Sort.Direction.ASC, "name")).stream()
                .map(actionMapper::toDto)
                .toList();
    }

    public ActionDTO create(ActionRequest request) {
        String name = normalize(request.getName());
        if (actionRepository.existsByNameIgnoreCase(name)) {
            throw duplicateAction(name);
        }
        Action action = new Action();
        action.setName(name);
        return actionMapper.toDto(actionRepository.save(action));
    }

    public ActionDTO update(UUID id, ActionRequest request) {
        Action action = actionRepository.findById(id).orElseThrow(() -> actionNotFound(id));
        String name = normalize(request.getName());
        if (!action.getName().equalsIgnoreCase(name) && actionRepository.existsByNameIgnoreCase(name)) {
            throw duplicateAction(name);
        }
        action.setName(name);
        return actionMapper.toDto(actionRepository.save(action));
    }

    public void delete(UUID id) {
        Action action = actionRepository.findById(id).orElseThrow(() -> actionNotFound(id));
        if (permissionRepository.existsByActionId(action.getId())) {
            throw actionInUse(action.getName());
        }
        actionRepository.delete(action);
    }

    private String normalize(String value) {
        return value == null ? null : value.trim().toUpperCase(Locale.ROOT);
    }

    private JMException actionNotFound(UUID id) {
        Locale locale = LocaleContextHolder.getLocale();
        String message = messageSource.getMessage("actions.not-found", new Object[] { id }, locale);
        return new JMException(HttpStatus.NOT_FOUND.value(), ProblemType.INVALID_DATA.getUri(),
                ProblemType.INVALID_DATA.getTitle(), message);
    }

    private JMException duplicateAction(String name) {
        Locale locale = LocaleContextHolder.getLocale();
        String message = messageSource.getMessage("actions.duplicate", new Object[] { name }, locale);
        return new JMException(HttpStatus.BAD_REQUEST.value(), ProblemType.INVALID_DATA.getUri(),
                ProblemType.INVALID_DATA.getTitle(), message);
    }

    private JMException actionInUse(String name) {
        Locale locale = LocaleContextHolder.getLocale();
        String message = messageSource.getMessage("actions.in-use", new Object[] { name }, locale);
        return new JMException(HttpStatus.CONFLICT.value(), ProblemType.INVALID_DATA.getUri(),
                ProblemType.INVALID_DATA.getTitle(), message);
    }
}
