package com.jm.services;

import com.jm.dto.ExerciseReferenceDTO;
import com.jm.mappers.ExerciseReferenceMapper;
import com.jm.repository.ExerciseReferenceRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class ExerciseReferenceService {

    private final ExerciseReferenceRepository repository;
    private final ExerciseReferenceMapper mapper;

    public ExerciseReferenceService(ExerciseReferenceRepository repository, ExerciseReferenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<ExerciseReferenceDTO> findAll(String language) {
        Sort sort = Sort.by(Sort.Order.asc("name"));
        if (StringUtils.hasText(language)) {
            var normalized = language.replace('_', '-');
            var languageTag = Locale.forLanguageTag(normalized).toLanguageTag();
            var references = repository.findByLanguageIgnoreCase(languageTag, sort);
            if (!references.isEmpty()) {
                return references.stream().map(mapper::toDTO).collect(Collectors.toList());
            }
        }
        return repository.findAll(sort).stream().map(mapper::toDTO).collect(Collectors.toList());
    }
}
