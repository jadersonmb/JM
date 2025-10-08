package com.jm.services;

import com.jm.dto.MealDTO;
import com.jm.entity.Meal;
import com.jm.repository.MealRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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

    private MealDTO toDto(Meal entity) {
        return MealDTO.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .name(entity.getName())
                .description(entity.getDescription())
                .typicalTime(entity.getTypicalTime())
                .sortOrder(entity.getSortOrder())
                .build();
    }
}
