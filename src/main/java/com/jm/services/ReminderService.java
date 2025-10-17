package com.jm.services;

import com.jm.dto.ReminderDTO;
import com.jm.dto.ReminderFilter;
import com.jm.dto.ReminderTargetDTO;
import com.jm.entity.Reminder;
import com.jm.entity.Users;
import com.jm.enums.ReminderPriority;
import com.jm.enums.ReminderType;
import com.jm.events.ReminderDueEvent;
import com.jm.execption.JMException;
import com.jm.execption.ProblemType;
import com.jm.mappers.ReminderMapper;
import com.jm.repository.ReminderRepository;
import com.jm.repository.UserRepository;
import com.jm.speciation.ReminderSpecification;
import com.jm.utils.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ReminderService {

    private static final Logger logger = LoggerFactory.getLogger(ReminderService.class);

    private final ReminderRepository repository;
    private final ReminderMapper mapper;
    private final UserRepository userRepository;
    private final MessageSource messageSource;
    private final ApplicationEventPublisher eventPublisher;

    public ReminderService(ReminderRepository repository, ReminderMapper mapper, UserRepository userRepository,
            MessageSource messageSource, ApplicationEventPublisher eventPublisher) {
        this.repository = repository;
        this.mapper = mapper;
        this.userRepository = userRepository;
        this.messageSource = messageSource;
        this.eventPublisher = eventPublisher;
    }

    @Transactional(readOnly = true)
    public Page<ReminderDTO> findAll(Pageable pageable, ReminderFilter filter) {
        ReminderFilter criteria = filter != null ? filter : new ReminderFilter();
        if (isClient()) {
            SecurityUtils.getCurrentUserId().ifPresent(criteria::setTargetUserId);
        }
        Page<Reminder> page = repository.findAll(ReminderSpecification.search(criteria), pageable);
        return page.map(mapper::toDTO);
    }

    @Transactional(readOnly = true)
    public ReminderDTO findById(UUID id) {
        Reminder entity = repository.findById(id).orElseThrow(this::reminderNotFound);
        enforceAccess(entity);
        return mapper.toDTO(entity);
    }

    @Transactional
    public ReminderDTO create(ReminderDTO dto) {
        return save(dto, false);
    }

    @Transactional
    public ReminderDTO update(UUID id, ReminderDTO dto) {
        if (id == null || dto == null) {
            throw invalidBody();
        }
        dto.setId(id);
        return save(dto, true);
    }

    @Transactional
    public void delete(UUID id) {
        Reminder entity = repository.findById(id).orElseThrow(this::reminderNotFound);
        enforceAccess(entity);
        repository.delete(entity);
        logger.debug("{} - {}", getMessage("reminder.deleted"), id);
    }

    @Transactional
    public ReminderDTO toggleActive(UUID id, boolean active) {
        Reminder entity = repository.findById(id).orElseThrow(this::reminderNotFound);
        enforceAccess(entity);
        entity.setActive(active);
        if (active) {
            entity.setCompleted(Boolean.FALSE);
            entity.setCompletedAt(null);
            entity.setDispatchedAt(null);
        }
        Reminder saved = repository.save(entity);
        return mapper.toDTO(saved);
    }

    @Transactional
    public ReminderDTO toggleCompleted(UUID id, boolean completed) {
        Reminder entity = repository.findById(id).orElseThrow(this::reminderNotFound);
        enforceAccess(entity);
        entity.setCompleted(completed);
        if (completed) {
            entity.setCompletedAt(LocalDateTime.now());
            entity.setActive(Boolean.FALSE);
        } else {
            entity.setCompletedAt(null);
        }
        Reminder saved = repository.save(entity);
        return mapper.toDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<ReminderTargetDTO> listTargets(String query) {
        if (isClient()) {
            UUID currentId = SecurityUtils.getCurrentUserId().orElseThrow(this::reminderForbidden);
            Users current = userRepository.findById(currentId).orElseThrow(this::reminderForbidden);
            return List.of(toTargetDto(current));
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
        return users.stream().map(this::toTargetDto).collect(Collectors.toList());
    }

    @Transactional
    public void dispatchDueReminders() {
        List<Reminder> due = repository.findDueReminders(LocalDateTime.now());
        if (due.isEmpty()) {
            return;
        }
        LocalDateTime dispatchMoment = LocalDateTime.now();
        for (Reminder reminder : due) {
            reminder.setDispatchedAt(dispatchMoment);
            repository.save(reminder);
            eventPublisher.publishEvent(new ReminderDueEvent(this, reminder.getId()));
        }
    }

    private ReminderDTO save(ReminderDTO dto, boolean updating) {
        if (dto == null || !StringUtils.hasText(dto.getTitle()) || dto.getScheduledAt() == null) {
            throw invalidBody();
        }

        Reminder entity;
        if (updating) {
            entity = repository.findById(dto.getId()).orElseThrow(this::reminderNotFound);
            enforceAccess(entity);
            mapper.updateEntityFromDto(dto, entity);
        } else {
            entity = mapper.toEntity(dto);
            entity.setActive(Boolean.TRUE);
            entity.setCompleted(Boolean.FALSE);
        }

        if (entity.getPriority() == null) {
            entity.setPriority(Optional.ofNullable(dto.getPriority()).orElse(ReminderPriority.MEDIUM));
        }
        if (entity.getType() == null) {
            entity.setType(Optional.ofNullable(dto.getType()).orElse(ReminderType.CUSTOM));
        }
        if (dto.getActive() != null) {
            entity.setActive(dto.getActive());
        }
        if (dto.getCompleted() != null) {
            entity.setCompleted(dto.getCompleted());
        }

        applyTargetUser(dto, entity);
        applyCreator(entity);

        Reminder saved = repository.save(entity);
        logger.debug("{} - {}", getMessage("reminder.saved"), saved.getId());
        return mapper.toDTO(saved);
    }

    private void applyTargetUser(ReminderDTO dto, Reminder entity) {
        UUID existingTarget = entity.getTargetUser() != null ? entity.getTargetUser().getId() : null;
        UUID targetId = dto != null && dto.getTargetUserId() != null ? dto.getTargetUserId() : existingTarget;
        if (isClient()) {
            UUID current = SecurityUtils.getCurrentUserId().orElseThrow(this::reminderForbidden);
            if (targetId != null && !current.equals(targetId)) {
                throw reminderForbidden();
            }
            Users currentUser = userRepository.findById(current).orElseThrow(this::reminderForbidden);
            entity.setTargetUser(currentUser);
            return;
        }

        if (targetId == null) {
            throw invalidBody();
        }
        Users target = userRepository.findById(targetId).orElseThrow(this::userNotFound);
        entity.setTargetUser(target);
    }

    private void applyCreator(Reminder entity) {
        if (entity.getCreatedBy() != null) {
            return;
        }
        UUID currentId = SecurityUtils.getCurrentUserId().orElse(null);
        if (currentId == null) {
            throw reminderForbidden();
        }
        Users creator = userRepository.findById(currentId).orElseThrow(this::userNotFound);
        entity.setCreatedBy(creator);
    }

    private ReminderTargetDTO toTargetDto(Users user) {
        StringBuilder name = new StringBuilder();
        if (StringUtils.hasText(user.getName())) {
            name.append(user.getName());
        }
        if (StringUtils.hasText(user.getLastName())) {
            if (!name.isEmpty()) {
                name.append(' ');
            }
            name.append(user.getLastName());
        }
        if (name.isEmpty() && StringUtils.hasText(user.getEmail())) {
            name.append(user.getEmail());
        }
        return ReminderTargetDTO.builder()
                .id(user.getId())
                .displayName(name.isEmpty() ? null : name.toString())
                .email(user.getEmail())
                .phone(user.getPhoneNumber())
                .build();
    }

    private boolean isClient() {
        return SecurityUtils.hasRole("CLIENT");
    }

    private JMException reminderNotFound() {
        String message = getMessage(ProblemType.REMINDER_NOT_FOUND.getMessageSource());
        return new JMException(HttpStatus.NOT_FOUND.value(), ProblemType.REMINDER_NOT_FOUND.getTitle(),
                ProblemType.REMINDER_NOT_FOUND.getUri(), message);
    }

    private JMException reminderForbidden() {
        String message = getMessage(ProblemType.REMINDER_FORBIDDEN.getMessageSource());
        return new JMException(HttpStatus.FORBIDDEN.value(), ProblemType.REMINDER_FORBIDDEN.getTitle(),
                ProblemType.REMINDER_FORBIDDEN.getUri(), message);
    }

    private JMException invalidBody() {
        String message = getMessage(ProblemType.INVALID_BODY.getMessageSource());
        return new JMException(HttpStatus.BAD_REQUEST.value(), ProblemType.INVALID_BODY.getTitle(),
                ProblemType.INVALID_BODY.getUri(), message);
    }

    private JMException userNotFound() {
        String message = getMessage(ProblemType.USER_NOT_FOUND.getMessageSource());
        return new JMException(HttpStatus.NOT_FOUND.value(), ProblemType.USER_NOT_FOUND.getTitle(),
                ProblemType.USER_NOT_FOUND.getUri(), message);
    }

    private String getMessage(String key, Object... args) {
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(key, args, locale);
    }

    private void enforceAccess(Reminder entity) {
        if (SecurityUtils.hasRole("ADMIN")) {
            return;
        }
        UUID currentId = SecurityUtils.getCurrentUserId().orElseThrow(this::reminderForbidden);
        UUID createdById = entity.getCreatedBy() != null ? entity.getCreatedBy().getId() : null;
        UUID targetId = entity.getTargetUser() != null ? entity.getTargetUser().getId() : null;
        if (!currentId.equals(createdById) && !currentId.equals(targetId)) {
            throw reminderForbidden();
        }
    }
}
