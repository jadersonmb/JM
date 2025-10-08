package com.jm.services;

import com.jm.dto.ProfessionDTO;
import com.jm.entity.Profession;
import com.jm.repository.ProfessionRepository;
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
public class ProfessionService {

    private final ProfessionRepository repository;

    public List<ProfessionDTO> findAll(String language) {
        Map<UUID, Profession> accumulator = new LinkedHashMap<>();
        if (StringUtils.hasText(language)) {
            repository.findByLanguageIgnoreCaseOrLanguageIsNullOrderByNameAsc(language)
                    .forEach(profession -> accumulator.put(profession.getId(), profession));
        } else {
            repository.findAllByOrderByNameAsc()
                    .forEach(profession -> accumulator.put(profession.getId(), profession));
        }
        return accumulator.values().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private ProfessionDTO toDto(Profession entity) {
        return ProfessionDTO.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .name(entity.getName())
                .description(entity.getDescription())
                .build();
    }
}
