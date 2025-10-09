package com.jm.services;

import com.jm.dto.UnitOfMeasureDTO;
import com.jm.entity.UnitOfMeasure;
import com.jm.execption.JMException;
import com.jm.execption.ProblemType;
import com.jm.mappers.DietPlanMapper;
import com.jm.repository.UnitOfMeasureRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UnitOfMeasureService {

    private static final Logger logger = LoggerFactory.getLogger(UnitOfMeasureService.class);

    private final UnitOfMeasureRepository repository;
    private final DietPlanMapper mapper;
    private final MessageSource messageSource;

    public UnitOfMeasureService(UnitOfMeasureRepository repository, DietPlanMapper mapper, MessageSource messageSource) {
        this.repository = repository;
        this.mapper = mapper;
        this.messageSource = messageSource;
    }

    public List<UnitOfMeasureDTO> findAll(Boolean onlyActive) {
        List<UnitOfMeasure> units;
        if (Boolean.TRUE.equals(onlyActive)) {
            units = repository.findByActiveTrueOrderByDisplayNameAsc();
        } else {
            units = repository.findAll(Sort.by(Sort.Direction.ASC, "displayName"));
        }
        logger.debug("Loaded {} units of measure", units.size());
        return units.stream().map(mapper::toUnitOfMeasureDTO).collect(Collectors.toList());
    }

    public UnitOfMeasureDTO findById(UUID id) {
        return mapper.toUnitOfMeasureDTO(repository.findById(id).orElseThrow(this::unitNotFound));
    }

    private JMException unitNotFound() {
        Locale locale = LocaleContextHolder.getLocale();
        String messageDetails = messageSource.getMessage(ProblemType.DIET_UNIT_NOT_FOUND.getMessageSource(),
                new Object[] { "" }, locale);
        return new JMException(HttpStatus.BAD_REQUEST.value(), ProblemType.DIET_UNIT_NOT_FOUND.getTitle(),
                ProblemType.DIET_UNIT_NOT_FOUND.getUri(), messageDetails);
    }
}

