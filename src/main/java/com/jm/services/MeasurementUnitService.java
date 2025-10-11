package com.jm.services;

import com.jm.dto.MeasurementUnitDTO;
import com.jm.entity.MeasurementUnits;
import com.jm.execption.JMException;
import com.jm.execption.ProblemType;
import com.jm.repository.MeasurementUnitRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
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
    private final MessageSource messageSource;

    public List<MeasurementUnitDTO> findAll(String language) {
        Map<UUID, MeasurementUnits> accumulator = new LinkedHashMap<>();
        if (StringUtils.hasText(language)) {
            repository.findByLanguageIgnoreCaseOrLanguageIsNullOrderByDescriptionAsc(language)
                    .forEach(unit -> accumulator.put(unit.getId(), unit));
        } else {
            repository.findAllByOrderByDescriptionAsc()
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
                .symbol(entity.getSymbol())
                .unitType(entity.getUnitType())
                .conversionFactor(entity.getConversionFactor())
                .baseUnit(entity.getBaseUnit())
                .description(entity.getDescription())
                .build();
    }

    public MeasurementUnits findByEntityId(UUID id) {
        return repository.findById(id).orElseThrow(this::measurementUnitsNotFound);
    }

    private JMException measurementUnitsNotFound() {
        ProblemType problemType = ProblemType.USER_NOT_FOUND;
        String messageDetails = messageSource.getMessage(problemType.getMessageSource(), new Object[] { "" },
                LocaleContextHolder.getLocale());
        return new JMException(HttpStatus.BAD_REQUEST.value(), problemType.getTitle(), problemType.getUri(),
                messageDetails);
    }
}
