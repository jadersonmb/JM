package com.jm.services;

import com.jm.dto.MeasurementUnitDTO;
import com.jm.entity.MeasurementUnits;
import com.jm.repository.MeasurementUnitRepository;
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
public class MeasurementUnitService {

    private final MeasurementUnitRepository repository;

    public List<MeasurementUnitDTO> findAll(String language) {
        Map<UUID, MeasurementUnits> accumulator = new LinkedHashMap<>();
        if (StringUtils.hasText(language)) {
            repository.findByLanguageIgnoreCaseOrLanguageIsNullOrderByNameAsc(language)
                    .forEach(unit -> accumulator.put(unit.getId(), unit));
        } else {
            repository.findAllByOrderByNameAsc()
                    .forEach(unit -> accumulator.put(unit.getId(), unit));
        }
        return accumulator.values().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private MeasurementUnitDTO toDto(MeasurementUnits entity) {
        return MeasurementUnitDTO.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .name(entity.getName())
                .symbol(entity.getSymbol())
                .unitType(entity.getUnitType())
                .conversionFactor(entity.getConversionFactor())
                .baseUnit(entity.getBaseUnit())
                .description(entity.getDescription())
                .build();
    }
}
