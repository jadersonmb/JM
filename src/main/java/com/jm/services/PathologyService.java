package com.jm.services;

import com.jm.dto.PathologyDTO;
import com.jm.entity.Pathology;
import com.jm.execption.JMException;
import com.jm.execption.ProblemType;
import com.jm.repository.PathologyRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PathologyService {

    private final PathologyRepository repository;
    private final MessageSource messageSource;

    public List<PathologyDTO> findAll(String language) {
        Map<UUID, Pathology> accumulator = new LinkedHashMap<>();
        if (StringUtils.hasText(language)) {
            repository.findByLanguageIgnoreCaseOrLanguageIsNullOrderByNameAsc(language)
                    .forEach(pathology -> accumulator.put(pathology.getId(), pathology));
        } else {
            repository.findAllByOrderByNameAsc()
                    .forEach(pathology -> accumulator.put(pathology.getId(), pathology));
        }
        return accumulator.values().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private PathologyDTO toDto(Pathology entity) {
        return PathologyDTO.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .name(entity.getName())
                .description(entity.getDescription())
                .category(entity.getCategory())
                .chronic(Boolean.TRUE.equals(entity.getIsChronic()))
                .requiresMonitoring(Boolean.TRUE.equals(entity.getRequiresMonitoring()))
                .build();
    }

    public Pathology findById(UUID id) {
        return repository.findById(id).orElseThrow(this::pathologyNotFound);
    }

    private JMException pathologyNotFound() {
        ProblemType problemType = ProblemType.PATHOLOGY_NOT_FOUND;
        String messageDetails = messageSource.getMessage(problemType.getMessageSource(), new Object[] { "" },
                LocaleContextHolder.getLocale());
        return new JMException(HttpStatus.BAD_REQUEST.value(), problemType.getTitle(), problemType.getUri(),
                messageDetails);
    }
}
