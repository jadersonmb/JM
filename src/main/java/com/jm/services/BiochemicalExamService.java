package com.jm.services;

import com.jm.dto.BiochemicalExamDTO;
import com.jm.entity.BiochemicalExam;
import com.jm.repository.BiochemicalExamRepository;
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
public class BiochemicalExamService {

    private final BiochemicalExamRepository repository;

    public List<BiochemicalExamDTO> findAll(String language) {
        Map<UUID, BiochemicalExam> accumulator = new LinkedHashMap<>();
        if (StringUtils.hasText(language)) {
            repository.findByLanguageIgnoreCaseOrLanguageIsNullOrderByNameAsc(language)
                    .forEach(exam -> accumulator.put(exam.getId(), exam));
        } else {
            repository.findAllByOrderByNameAsc()
                    .forEach(exam -> accumulator.put(exam.getId(), exam));
        }
        return accumulator.values().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private BiochemicalExamDTO toDto(BiochemicalExam entity) {
        return BiochemicalExamDTO.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .name(entity.getName())
                .description(entity.getDescription())
                .measurementUnitId(entity.getMeasurementUnit() != null ? entity.getMeasurementUnit().getId() : null)
                .measurementUnitName(entity.getMeasurementUnit() != null ? entity.getMeasurementUnit().getName() : null)
                .minReferenceValue(entity.getMinReferenceValue())
                .maxReferenceValue(entity.getMaxReferenceValue())
                .build();
    }
}
