package com.jm.services;

import com.jm.dto.UnitOfMeasureDTO;
import com.jm.entity.MeasurementUnits;
import com.jm.execption.JMException;
import com.jm.execption.ProblemType;
import com.jm.repository.MeasurementUnitRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UnitOfMeasureService {

    private static final Logger logger = LoggerFactory.getLogger(UnitOfMeasureService.class);

    private final MeasurementUnitRepository repository;
    private final MessageSource messageSource;

    public UnitOfMeasureService(MeasurementUnitRepository repository, MessageSource messageSource) {
        this.repository = repository;
        this.messageSource = messageSource;
    }

    @Transactional(readOnly = true)
    public List<UnitOfMeasureDTO> findAll(Boolean onlyActive) {
        List<MeasurementUnits> units;
        if (Boolean.TRUE.equals(onlyActive)) {
            units = repository.findByIsActiveTrueOrderByDescriptionAsc();
        } else {
            units = repository.findAllByOrderByDescriptionAsc();
        }
        logger.debug("Loaded {} measurement units", units.size());
        return units.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UnitOfMeasureDTO findById(UUID id) {
        return toDto(repository.findById(id).orElseThrow(this::unitNotFound));
    }

    private UnitOfMeasureDTO toDto(MeasurementUnits unit) {
        return UnitOfMeasureDTO.builder()
                .id(unit.getId())
                .code(unit.getCode())
                .displayName(unit.getDescription() != null ? unit.getDescription() : unit.getSymbol())
                .active(unit.getIsActive())
                .build();
    }

    private JMException unitNotFound() {
        Locale locale = LocaleContextHolder.getLocale();
        String messageDetails = messageSource.getMessage(ProblemType.DIET_UNIT_NOT_FOUND.getMessageSource(),
                new Object[] { "" }, locale);
        return new JMException(HttpStatus.BAD_REQUEST.value(), ProblemType.DIET_UNIT_NOT_FOUND.getTitle(),
                ProblemType.DIET_UNIT_NOT_FOUND.getUri(), messageDetails);
    }
}

