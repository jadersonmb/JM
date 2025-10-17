package com.jm.services;

import com.jm.dto.ExerciseDTO;
import com.jm.dto.ExerciseFilter;
import com.jm.entity.Exercise;
import com.jm.entity.ExerciseReference;
import com.jm.entity.Users;
import com.jm.execption.JMException;
import com.jm.execption.ProblemType;
import com.jm.mappers.ExerciseMapper;
import com.jm.repository.ExerciseReferenceRepository;
import com.jm.repository.ExerciseRepository;
import com.jm.repository.UserRepository;
import com.jm.speciation.ExerciseSpecification;
import com.jm.utils.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class ExerciseService {

    private static final Logger logger = LoggerFactory.getLogger(ExerciseService.class);

    private final ExerciseRepository exerciseRepository;
    private final ExerciseReferenceRepository exerciseReferenceRepository;
    private final ExerciseMapper exerciseMapper;
    private final UserRepository userRepository;
    private final MessageSource messageSource;

    public ExerciseService(ExerciseRepository exerciseRepository, ExerciseReferenceRepository exerciseReferenceRepository,
            ExerciseMapper exerciseMapper, UserRepository userRepository, MessageSource messageSource) {
        this.exerciseRepository = exerciseRepository;
        this.exerciseReferenceRepository = exerciseReferenceRepository;
        this.exerciseMapper = exerciseMapper;
        this.userRepository = userRepository;
        this.messageSource = messageSource;
    }

    public Page<ExerciseDTO> findAll(Pageable pageable, ExerciseFilter filter) {
        ExerciseFilter criteria = filter;
        if (criteria == null) {
            criteria = new ExerciseFilter();
        }

        if (!SecurityUtils.hasRole("ADMIN")) {
            UUID currentUserId = SecurityUtils.getCurrentUserId().orElseThrow(this::exerciseForbidden);
            criteria.setUserId(currentUserId);
        }

        return exerciseRepository.findAll(ExerciseSpecification.search(criteria), pageable)
                .map(exerciseMapper::toDTO);
    }

    public ExerciseDTO findById(UUID id) {
        Exercise exercise = exerciseRepository.findById(id).orElseThrow(this::exerciseNotFound);
        ensureOwnership(exercise);
        return exerciseMapper.toDTO(exercise);
    }

    public ExerciseDTO save(ExerciseDTO dto) {
        Exercise entity;
        if (dto.getId() != null) {
            entity = exerciseRepository.findById(dto.getId()).orElseThrow(this::exerciseNotFound);
            ensureOwnership(entity);
            exerciseMapper.updateEntityFromDto(dto, entity);
        } else {
            entity = exerciseMapper.toEntity(dto);
        }

        applyReference(entity, dto.getReferenceId());
        applyUser(entity, dto.getUserId());

        Exercise saved = exerciseRepository.save(entity);
        logger.debug("Exercise {} saved with id {}", saved.getCustomName(), saved.getId());
        return exerciseMapper.toDTO(saved);
    }

    public void delete(UUID id) {
        Exercise entity = exerciseRepository.findById(id).orElseThrow(this::exerciseNotFound);
        ensureOwnership(entity);
        exerciseRepository.delete(entity);
    }

    private void applyReference(Exercise entity, UUID referenceId) {
        if (referenceId == null) {
            entity.setReference(null);
            return;
        }
        ExerciseReference reference = exerciseReferenceRepository.findById(referenceId)
                .orElseThrow(this::exerciseReferenceNotFound);
        entity.setReference(reference);
    }

    private JMException exerciseNotFound() {
        ProblemType problemType = ProblemType.EXERCISE_NOT_FOUND;
        String message = messageSource.getMessage(problemType.getMessageSource(), new Object[] { "" },
                LocaleContextHolder.getLocale());
        return new JMException(HttpStatus.NOT_FOUND.value(), problemType.getUri(), problemType.getTitle(), message);
    }

    private JMException exerciseReferenceNotFound() {
        ProblemType problemType = ProblemType.EXERCISE_REFERENCE_NOT_FOUND;
        String message = messageSource.getMessage(problemType.getMessageSource(), new Object[] { "" },
                LocaleContextHolder.getLocale());
        return new JMException(HttpStatus.BAD_REQUEST.value(), problemType.getUri(), problemType.getTitle(), message);
    }

    private JMException exerciseForbidden() {
        ProblemType problemType = ProblemType.EXERCISE_FORBIDDEN;
        String message = messageSource.getMessage(problemType.getMessageSource(), new Object[] { "" },
                LocaleContextHolder.getLocale());
        return new JMException(HttpStatus.FORBIDDEN.value(), problemType.getUri(), problemType.getTitle(), message);
    }

    private JMException userNotFound() {
        ProblemType problemType = ProblemType.USER_NOT_FOUND;
        String message = messageSource.getMessage(problemType.getMessageSource(), new Object[] { "" },
                LocaleContextHolder.getLocale());
        return new JMException(HttpStatus.BAD_REQUEST.value(), problemType.getUri(), problemType.getTitle(), message);
    }

    private void ensureOwnership(Exercise exercise) {
        if (exercise == null || SecurityUtils.hasRole("ADMIN")) {
            return;
        }
        UUID currentUserId = SecurityUtils.getCurrentUserId().orElseThrow(this::exerciseForbidden);
        UUID ownerId = Optional.ofNullable(exercise.getUser()).map(Users::getId).orElse(null);
        if (ownerId == null || !ownerId.equals(currentUserId)) {
            throw exerciseForbidden();
        }
    }

    private void applyUser(Exercise entity, UUID userId) {
        if (SecurityUtils.hasRole("ADMIN")) {
            if (userId == null) {
                entity.setUser(null);
                return;
            }
            Users target = userRepository.findById(userId).orElseThrow(this::userNotFound);
            entity.setUser(target);
            return;
        }

        UUID currentUserId = SecurityUtils.getCurrentUserId().orElseThrow(this::exerciseForbidden);
        Users currentUser = userRepository.findById(currentUserId).orElseThrow(this::userNotFound);
        entity.setUser(currentUser);
    }
}
