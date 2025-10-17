package com.jm.services;

import com.jm.dto.ExerciseReferenceDTO;
import com.jm.entity.ExerciseReference;
import com.jm.execption.JMException;
import com.jm.execption.ProblemType;
import com.jm.mappers.ExerciseReferenceMapper;
import com.jm.repository.ExerciseReferenceRepository;
import com.jm.speciation.ExerciseReferenceSpecification;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class ExerciseReferenceService {

    private final ExerciseReferenceRepository repository;
    private final ExerciseReferenceMapper mapper;
    private final MessageSource messageSource;

    @Transactional(readOnly = true)
    public List<ExerciseReferenceDTO> findAll(String language) {
        Sort sort = Sort.by(Sort.Order.asc("name"));
        String normalizedLanguage = normalizeLanguageTag(language);
        if (StringUtils.hasText(normalizedLanguage)) {
            var references = repository.findByLanguageIgnoreCase(normalizedLanguage, sort);
            if (!references.isEmpty()) {
                return references.stream().map(mapper::toDTO).collect(Collectors.toList());
            }
        }
        return repository.findAll(sort).stream().map(mapper::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<ExerciseReferenceDTO> search(Pageable pageable, String search, String language, String muscleGroup,
            String equipment) {
        String normalizedLanguage = normalizeLanguageTag(language);
        String languageFilter = StringUtils.hasText(normalizedLanguage)
                ? normalizedLanguage.toLowerCase(Locale.ROOT)
                : null;
        return repository
                .findAll(ExerciseReferenceSpecification.search(search, languageFilter, muscleGroup, equipment), pageable)
                .map(mapper::toDTO);
    }

    @Transactional
    public ExerciseReferenceDTO save(ExerciseReferenceDTO dto) {
        ExerciseReference entity;
        if (dto.getId() != null) {
            entity = repository.findById(dto.getId()).orElseThrow(this::exerciseReferenceNotFound);
            mapper.updateEntityFromDto(dto, entity);
        } else {
            entity = mapper.toEntity(dto);
        }

        entity.setLanguage(normalizeLanguageTag(dto.getLanguage()));

        if (StringUtils.hasText(entity.getCode())) {
            entity.setCode(entity.getCode().trim().toUpperCase(Locale.ROOT));
        }
        if (StringUtils.hasText(entity.getName())) {
            entity.setName(entity.getName().trim());
        }
        if (StringUtils.hasText(entity.getDescription())) {
            entity.setDescription(entity.getDescription().trim());
        }
        if (StringUtils.hasText(entity.getMuscleGroup())) {
            entity.setMuscleGroup(entity.getMuscleGroup().trim());
        }
        if (StringUtils.hasText(entity.getEquipment())) {
            entity.setEquipment(entity.getEquipment().trim());
        }

        ExerciseReference saved = repository.save(entity);
        return mapper.toDTO(saved);
    }

    @Transactional
    public void delete(UUID id) {
        ExerciseReference entity = repository.findById(id).orElseThrow(this::exerciseReferenceNotFound);
        repository.delete(entity);
    }

    @Transactional(readOnly = true)
    public ExerciseReferenceDTO findById(UUID id) {
        return mapper.toDTO(repository.findById(id).orElseThrow(this::exerciseReferenceNotFound));
    }

    private JMException exerciseReferenceNotFound() {
        ProblemType problemType = ProblemType.EXERCISE_REFERENCE_NOT_FOUND;
        Locale locale = LocaleContextHolder.getLocale();
        String message = messageSource.getMessage(problemType.getMessageSource(), new Object[] { "" }, locale);
        return new JMException(HttpStatus.BAD_REQUEST.value(), problemType.getTitle(),
                problemType.getUri(), message);
    }

    private String normalizeLanguageTag(String language) {
        if (!StringUtils.hasText(language)) {
            return null;
        }
        String normalized = language.trim().replace('_', '-');
        Locale locale = Locale.forLanguageTag(normalized);
        if (!StringUtils.hasText(locale.getLanguage())) {
            return normalized;
        }
        String tag = locale.toLanguageTag();
        return StringUtils.hasText(tag) ? tag : normalized;
    }
}
