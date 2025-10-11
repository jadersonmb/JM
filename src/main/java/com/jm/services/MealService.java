package com.jm.services;

import com.jm.dto.MealDTO;
import com.jm.entity.Meal;
import com.jm.execption.JMException;
import com.jm.execption.ProblemType;
import com.jm.repository.MealRepository;
import com.jm.speciation.MealSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Locale;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MealService {

    private final MealRepository repository;

    public List<MealDTO> findAll(String language) {
        Map<UUID, Meal> accumulator = new LinkedHashMap<>();
        if (StringUtils.hasText(language)) {
            repository.findByLanguageIgnoreCaseOrLanguageIsNullOrderBySortOrderAscNameAsc(language)
                    .forEach(meal -> accumulator.put(meal.getId(), meal));
        } else {
            repository.findAllByOrderBySortOrderAscNameAsc()
                    .forEach(meal -> accumulator.put(meal.getId(), meal));
        }
        return accumulator.values().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public Page<MealDTO> search(Pageable pageable, String name, String code, String language) {
        return repository.findAll(MealSpecification.search(name, code, language), pageable)
                .map(this::toDto);
    }

    @Transactional
    public MealDTO save(MealDTO dto) {
        Meal entity;
        if (dto.getId() != null) {
            entity = repository.findById(dto.getId()).orElseThrow(this::mealNotFound);
        } else {
            entity = new Meal();
        }

        entity.setCode(StringUtils.hasText(dto.getCode()) ? dto.getCode().trim().toUpperCase(Locale.ROOT) : null);
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setTypicalTime(dto.getTypicalTime());
        entity.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        entity.setLanguage(StringUtils.hasText(dto.getLanguage()) ? dto.getLanguage().trim().toLowerCase(Locale.ROOT) : null);

        Meal saved = repository.save(entity);
        return toDto(saved);
    }

    @Transactional
    public void delete(UUID id) {
        Meal entity = repository.findById(id).orElseThrow(this::mealNotFound);
        repository.delete(entity);
    }

    private MealDTO toDto(Meal entity) {
        return MealDTO.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .name(entity.getName())
                .description(entity.getDescription())
                .typicalTime(entity.getTypicalTime())
                .sortOrder(entity.getSortOrder())
                .language(entity.getLanguage())
                .build();
    }

    private JMException mealNotFound() {
        return new JMException(HttpStatus.BAD_REQUEST.value(), ProblemType.INVALID_DATA.getUri(),
                ProblemType.INVALID_DATA.getTitle(), "Meal not found");
    }
}
