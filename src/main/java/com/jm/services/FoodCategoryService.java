package com.jm.services;

import com.jm.dto.FoodCategoryDTO;
import com.jm.entity.FoodCategory;
import com.jm.repository.FoodCategoryRepository;
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
public class FoodCategoryService {

    private final FoodCategoryRepository repository;

    public List<FoodCategoryDTO> findAll(String language) {
        Map<UUID, FoodCategory> accumulator = new LinkedHashMap<>();
        if (StringUtils.hasText(language)) {
            repository.findByLanguageIgnoreCaseOrLanguageIsNullOrderByNameAsc(language)
                    .forEach(category -> accumulator.put(category.getId(), category));
        } else {
            repository.findAllByOrderByNameAsc()
                    .forEach(category -> accumulator.put(category.getId(), category));
        }
        return accumulator.values().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private FoodCategoryDTO toDto(FoodCategory entity) {
        return FoodCategoryDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .color(entity.getColor())
                .build();
    }
}
