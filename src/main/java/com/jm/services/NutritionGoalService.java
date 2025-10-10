package com.jm.services;

import com.jm.dto.NutritionGoalDTO;
import com.jm.dto.NutritionGoalOwnerDTO;
import com.jm.entity.MeasurementUnits;
import com.jm.entity.NutritionGoal;
import com.jm.entity.NutritionGoalTemplate;
import com.jm.entity.Users;
import com.jm.enums.NutritionGoalPeriodicity;
import com.jm.enums.NutritionGoalTargetMode;
import com.jm.execption.JMException;
import com.jm.execption.ProblemType;
import com.jm.mappers.NutritionGoalMapper;
import com.jm.repository.MeasurementUnitRepository;
import com.jm.repository.NutritionGoalRepository;
import com.jm.repository.NutritionGoalTemplateRepository;
import com.jm.repository.UserRepository;
import com.jm.speciation.NutritionGoalSpecification;
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
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class NutritionGoalService {

    private static final Logger logger = LoggerFactory.getLogger(NutritionGoalService.class);

    private final NutritionGoalRepository repository;
    private final NutritionGoalMapper mapper;
    private final NutritionGoalTemplateRepository templateRepository;
    private final MeasurementUnitRepository measurementUnitRepository;
    private final UserRepository userRepository;
    private final MessageSource messageSource;

    public NutritionGoalService(NutritionGoalRepository repository, NutritionGoalMapper mapper,
            NutritionGoalTemplateRepository templateRepository, MeasurementUnitRepository measurementUnitRepository,
            UserRepository userRepository, MessageSource messageSource) {
        this.repository = repository;
        this.mapper = mapper;
        this.templateRepository = templateRepository;
        this.measurementUnitRepository = measurementUnitRepository;
        this.userRepository = userRepository;
        this.messageSource = messageSource;
    }

    @Transactional(readOnly = true)
    public Page<NutritionGoalDTO> findAll(Pageable pageable, NutritionGoalDTO filter) {
        NutritionGoalDTO criteria = filter != null ? filter : new NutritionGoalDTO();
        if (isClient()) {
            SecurityUtils.getCurrentUserId().ifPresent(criteria::setCreatedByUserId);
        }
        Page<NutritionGoal> page = repository.findAll(NutritionGoalSpecification.search(criteria), pageable);
        return page.map(mapper::toDTO);
    }

    @Transactional(readOnly = true)
    public NutritionGoalDTO findById(UUID id) {
        NutritionGoal entity = repository.findById(id).orElseThrow(this::goalNotFound);
        enforceOwnership(entity);
        return mapper.toDTO(entity);
    }

    @Transactional
    public NutritionGoalDTO create(NutritionGoalDTO dto) {
        return save(dto, false);
    }

    @Transactional
    public NutritionGoalDTO update(NutritionGoalDTO dto) {
        if (dto == null || dto.getId() == null) {
            throw invalidBodyException();
        }
        return save(dto, true);
    }

    @Transactional
    public void delete(UUID id) {
        NutritionGoal entity = repository.findById(id).orElseThrow(this::goalNotFound);
        enforceOwnership(entity);
        repository.delete(entity);
        logger.debug("{} - {}", getMessage("goal.deleted"), id);
    }

    @Transactional(readOnly = true)
    public List<NutritionGoalOwnerDTO> listOwners(String query) {
        if (isClient()) {
            UUID currentUserId = SecurityUtils.getCurrentUserId().orElseThrow(this::invalidBodyException);
            Users currentUser = userRepository.findById(currentUserId).orElseThrow(this::goalForbidden);
            return List.of(toOwnerDto(currentUser));
        }

        List<Users> users;
        if (StringUtils.hasText(query)) {
            Map<UUID, Users> accumulator = new LinkedHashMap<>();
            userRepository.findTop20ByTypeAndNameContainingIgnoreCaseOrderByNameAsc(Users.Type.CLIENT, query)
                    .forEach(user -> accumulator.put(user.getId(), user));
            userRepository.findTop20ByTypeAndEmailContainingIgnoreCaseOrderByEmailAsc(Users.Type.CLIENT, query)
                    .forEach(user -> accumulator.putIfAbsent(user.getId(), user));
            users = new ArrayList<>(accumulator.values());
        } else {
            users = userRepository.findTop50ByTypeOrderByNameAsc(Users.Type.CLIENT);
        }

        return users.stream().map(this::toOwnerDto).collect(Collectors.toList());
    }

    private NutritionGoalDTO save(NutritionGoalDTO dto, boolean updating) {
        if (dto == null) {
            throw invalidBodyException();
        }

        NutritionGoal entity;
        if (updating) {
            entity = repository.findById(dto.getId()).orElseThrow(this::goalNotFound);
            enforceOwnership(entity);
            mapper.updateEntityFromDto(dto, entity);
        } else {
            entity = mapper.toEntity(dto);
            entity.setActive(Boolean.TRUE);
        }

        applyOwner(dto, entity);
        applyUnit(dto, entity);
        applyTemplate(dto, entity);

        if (dto.getTargetMode() != null) {
            entity.setTargetMode(dto.getTargetMode());
        } else if (entity.getTargetMode() == null) {
            entity.setTargetMode(NutritionGoalTargetMode.ABSOLUTE);
        }

        if (dto.getPeriodicity() != null) {
            entity.setPeriodicity(dto.getPeriodicity());
        }

        if (entity.getPeriodicity() == null) {
            entity.setPeriodicity(NutritionGoalPeriodicity.DAILY);
        }

        if (entity.getPeriodicity() != null && entity.getPeriodicity() != NutritionGoalPeriodicity.CUSTOM) {
            entity.setCustomPeriodDays(null);
        }

        if (dto.getCustomPeriodDays() != null && entity.getPeriodicity() == NutritionGoalPeriodicity.CUSTOM) {
            entity.setCustomPeriodDays(dto.getCustomPeriodDays());
        }

        if (entity.getActive() == null) {
            entity.setActive(Boolean.TRUE);
        }
        if (dto.getActive() != null) {
            entity.setActive(dto.getActive());
        }

        NutritionGoal saved = repository.save(entity);
        logger.debug("{} - {}", getMessage("goal.saved"), saved.getId());
        return mapper.toDTO(saved);
    }

    private void applyOwner(NutritionGoalDTO dto, NutritionGoal entity) {
        UUID ownerId = dto.getCreatedByUserId();
        if (isClient()) {
            UUID currentUserId = SecurityUtils.getCurrentUserId().orElseThrow(this::goalForbidden);
            if (ownerId != null && !ownerId.equals(currentUserId)) {
                throw goalForbidden();
            }
            Users currentUser = userRepository.findById(currentUserId).orElseThrow(this::goalForbidden);
            entity.setCreatedBy(currentUser);
            return;
        }

        UUID resolvedOwnerId = Optional.ofNullable(ownerId)
                .or(() -> SecurityUtils.getCurrentUserId())
                .orElse(null);
        if (resolvedOwnerId == null) {
            throw invalidBodyException();
        }
        Users owner = userRepository.findById(resolvedOwnerId).orElseThrow(this::userNotFound);
        entity.setCreatedBy(owner);
    }

    private void applyUnit(NutritionGoalDTO dto, NutritionGoal entity) {
        if (dto.getUnitId() == null) {
            throw invalidBodyException();
        }
        MeasurementUnits unit = measurementUnitRepository.findById(dto.getUnitId())
                .orElseThrow(this::unitNotFound);
        entity.setUnit(unit);
    }

    private void applyTemplate(NutritionGoalDTO dto, NutritionGoal entity) {
        if (dto.getTemplateId() == null) {
            entity.setTemplate(null);
            return;
        }
        NutritionGoalTemplate template = templateRepository.findById(dto.getTemplateId())
                .orElseThrow(this::templateNotFound);
        entity.setTemplate(template);
    }

    private void enforceOwnership(NutritionGoal entity) {
        if (!isClient()) {
            return;
        }
        UUID currentUserId = SecurityUtils.getCurrentUserId().orElse(null);
        if (currentUserId == null) {
            throw goalForbidden();
        }
        UUID ownerId = entity.getCreatedBy() != null ? entity.getCreatedBy().getId() : null;
        if (!Objects.equals(currentUserId, ownerId)) {
            throw goalForbidden();
        }
    }

    private NutritionGoalOwnerDTO toOwnerDto(Users user) {
        if (user == null) {
            return null;
        }
        String displayName = user.getName();
        if (StringUtils.hasText(user.getLastName())) {
            displayName = (displayName != null ? displayName + " " : "") + user.getLastName();
        }
        if (!StringUtils.hasText(displayName)) {
            displayName = user.getEmail();
        }
        return NutritionGoalOwnerDTO.builder()
                .id(user.getId())
                .displayName(displayName)
                .email(user.getEmail())
                .build();
    }

    private JMException goalNotFound() {
        return buildException(ProblemType.NUTRITION_GOAL_NOT_FOUND, HttpStatus.BAD_REQUEST);
    }

    private JMException goalForbidden() {
        return buildException(ProblemType.NUTRITION_GOAL_FORBIDDEN, HttpStatus.FORBIDDEN);
    }

    private JMException templateNotFound() {
        return buildException(ProblemType.NUTRITION_GOAL_TEMPLATE_NOT_FOUND, HttpStatus.BAD_REQUEST);
    }

    private JMException unitNotFound() {
        return buildException(ProblemType.NUTRITION_GOAL_UNIT_NOT_FOUND, HttpStatus.BAD_REQUEST);
    }

    private JMException userNotFound() {
        return buildException(ProblemType.USER_NOT_FOUND, HttpStatus.BAD_REQUEST);
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

    private boolean isClient() {
        return SecurityUtils.hasRole("CLIENT");
    }
}
