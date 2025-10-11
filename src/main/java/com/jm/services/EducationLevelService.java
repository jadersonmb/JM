package com.jm.services;

import com.jm.dto.EducationLevelDTO;
import com.jm.entity.EducationLevel;
import com.jm.execption.JMException;
import com.jm.execption.ProblemType;
import com.jm.mappers.EducationLevelMapper;
import com.jm.repository.EducationLevelRepository;
import com.jm.speciation.EducationLevelSpecification;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class EducationLevelService {

    private final EducationLevelRepository repository;
    private final EducationLevelMapper mapper;
    private final MessageSource messageSource;

    public List<EducationLevelDTO> findAll(String language) {
        Map<UUID, EducationLevel> accumulator = new LinkedHashMap<>();
        if (StringUtils.hasText(language)) {
            repository.findByLanguageIgnoreCaseOrLanguageIsNullOrderByNameAsc(language)
                    .forEach(level -> accumulator.put(level.getId(), level));
        } else {
            repository.findAllByOrderByNameAsc()
                    .forEach(level -> accumulator.put(level.getId(), level));
        }
        return accumulator.values().stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    public Page<EducationLevelDTO> search(Pageable pageable, String name, String code, String language) {
        return repository.findAll(EducationLevelSpecification.search(name, code, language), pageable)
                .map(mapper::toDTO);
    }

    @Transactional
    public EducationLevelDTO save(EducationLevelDTO dto) {
        EducationLevel entity;
        if (dto.getId() != null) {
            entity = repository.findById(dto.getId()).orElseThrow(this::educationLevelNotFound);
            mapper.updateEntityFromDto(dto, entity);
        } else {
            entity = mapper.toEntity(dto);
        }
        EducationLevel saved = repository.save(entity);
        return mapper.toDTO(saved);
    }

    @Transactional
    public void delete(UUID id) {
        EducationLevel entity = repository.findById(id).orElseThrow(this::educationLevelNotFound);
        repository.delete(entity);
    }

    public EducationLevelDTO findById(UUID id) {
        return mapper.toDTO(repository.findById(id).orElseThrow(this::educationLevelNotFound));
    }

    public EducationLevel findEntityById(UUID id) {
        return repository.findById(id).orElseThrow(this::educationLevelNotFound);
    }

    private JMException educationLevelNotFound() {
        ProblemType problemType = ProblemType.EDUCATION_LEVEL_NOT_FOUND;
        Locale locale = LocaleContextHolder.getLocale();
        String message = messageSource.getMessage(problemType.getMessageSource(), new Object[] { "" }, locale);
        return new JMException(HttpStatus.BAD_REQUEST.value(), problemType.getTitle(), problemType.getUri(), message);
    }
}
