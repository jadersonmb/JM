package com.jm.services;

import com.jm.dto.EducationLevelDTO;
import com.jm.entity.EducationLevel;
import com.jm.repository.EducationLevelRepository;
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
public class EducationLevelService {

    private final EducationLevelRepository repository;

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
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private EducationLevelDTO toDto(EducationLevel entity) {
        return EducationLevelDTO.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .name(entity.getName())
                .description(entity.getDescription())
                .build();
    }
}
