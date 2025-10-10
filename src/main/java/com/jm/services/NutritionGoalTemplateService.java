package com.jm.services;

import com.jm.dto.NutritionGoalTemplateDTO;
import com.jm.entity.MeasurementUnits;
import com.jm.entity.NutritionGoalTemplate;
import com.jm.execption.JMException;
import com.jm.execption.ProblemType;
import com.jm.mappers.NutritionGoalTemplateMapper;
import com.jm.repository.MeasurementUnitRepository;
import com.jm.repository.NutritionGoalTemplateRepository;
import com.jm.speciation.NutritionGoalTemplateSpecification;
import com.jm.utils.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;

@Service
public class NutritionGoalTemplateService {

    private static final Logger logger = LoggerFactory.getLogger(NutritionGoalTemplateService.class);

    private final NutritionGoalTemplateRepository repository;
    private final NutritionGoalTemplateMapper mapper;
    private final MeasurementUnitRepository measurementUnitRepository;
    private final MessageSource messageSource;

    public NutritionGoalTemplateService(NutritionGoalTemplateRepository repository,
            NutritionGoalTemplateMapper mapper, MeasurementUnitRepository measurementUnitRepository,
            MessageSource messageSource) {
        this.repository = repository;
        this.mapper = mapper;
        this.measurementUnitRepository = measurementUnitRepository;
        this.messageSource = messageSource;
    }

    @Transactional(readOnly = true)
    public Page<NutritionGoalTemplateDTO> findAll(Pageable pageable, NutritionGoalTemplateDTO filter) {
        Page<NutritionGoalTemplate> page = repository
                .findAll(NutritionGoalTemplateSpecification.search(filter), pageable);
        return page.map(mapper::toDTO);
    }

    @Transactional(readOnly = true)
    public NutritionGoalTemplateDTO findById(UUID id) {
        NutritionGoalTemplate entity = repository.findById(id).orElseThrow(this::templateNotFound);
        return mapper.toDTO(entity);
    }

    @Transactional
    public NutritionGoalTemplateDTO create(NutritionGoalTemplateDTO dto) {
        enforceAdmin();
        return save(dto, false);
    }

    @Transactional
    public NutritionGoalTemplateDTO update(NutritionGoalTemplateDTO dto) {
        enforceAdmin();
        if (dto == null || dto.getId() == null) {
            throw invalidBodyException();
        }
        return save(dto, true);
    }

    @Transactional
    public void delete(UUID id) {
        enforceAdmin();
        NutritionGoalTemplate entity = repository.findById(id).orElseThrow(this::templateNotFound);
        repository.delete(entity);
        logger.debug("{} - {}", getMessage("goal.template.deleted"), id);
    }

    private NutritionGoalTemplateDTO save(NutritionGoalTemplateDTO dto, boolean updating) {
        if (dto == null) {
            throw invalidBodyException();
        }

        NutritionGoalTemplate entity;
        if (updating) {
            entity = repository.findById(dto.getId()).orElseThrow(this::templateNotFound);
            mapper.updateEntityFromDto(dto, entity);
        } else {
            entity = mapper.toEntity(dto);
            entity.setActive(Boolean.TRUE);
        }

        applyUnit(dto, entity);

        if (dto.getActive() != null) {
            entity.setActive(dto.getActive());
        } else if (entity.getActive() == null) {
            entity.setActive(Boolean.TRUE);
        }

        NutritionGoalTemplate saved = repository.save(entity);
        logger.debug("{} - {}", getMessage("goal.template.saved"), saved.getId());
        return mapper.toDTO(saved);
    }

    private void applyUnit(NutritionGoalTemplateDTO dto, NutritionGoalTemplate entity) {
        if (dto.getUnitId() == null) {
            throw invalidBodyException();
        }
        MeasurementUnits unit = measurementUnitRepository.findById(dto.getUnitId())
                .orElseThrow(this::unitNotFound);
        entity.setUnit(unit);
    }

    private void enforceAdmin() {
        if (!SecurityUtils.hasRole("ADMIN")) {
            throw goalForbidden();
        }
    }

    private JMException templateNotFound() {
        return buildException(ProblemType.NUTRITION_GOAL_TEMPLATE_NOT_FOUND, HttpStatus.BAD_REQUEST);
    }

    private JMException goalForbidden() {
        return buildException(ProblemType.NUTRITION_GOAL_FORBIDDEN, HttpStatus.FORBIDDEN);
    }

    private JMException unitNotFound() {
        return buildException(ProblemType.NUTRITION_GOAL_UNIT_NOT_FOUND, HttpStatus.BAD_REQUEST);
    }

    private JMException invalidBodyException() {
        return buildException(ProblemType.INVALID_BODY, HttpStatus.BAD_REQUEST);
    }

    private JMException buildException(ProblemType type, HttpStatus status) {
        Locale locale = LocaleContextHolder.getLocale();
        String messageDetails = messageSource.getMessage(type.getMessageSource(), new Object[] { "" }, locale);
        return new JMException(status.value(), type.getTitle(), type.getUri(), messageDetails);
    }

    private String getMessage(String key) {
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(key, new Object[] { "" }, locale);
    }
}
