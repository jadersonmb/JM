package com.jm.services;

import com.jm.dto.PhotoEvolutionDTO;
import com.jm.dto.PhotoEvolutionOwnerDTO;
import com.jm.entity.Image;
import com.jm.entity.PhotoEvolution;
import com.jm.entity.Users;
import com.jm.enums.BodyPart;
import com.jm.execption.JMException;
import com.jm.execption.ProblemType;
import com.jm.mappers.PhotoEvolutionMapper;
import com.jm.repository.PhotoEvolutionRepository;
import com.jm.repository.UserRepository;
import com.jm.utils.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PhotoEvolutionService {

    private static final Logger logger = LoggerFactory.getLogger(PhotoEvolutionService.class);

    private final PhotoEvolutionRepository repository;
    private final PhotoEvolutionMapper mapper;
    private final UserRepository userRepository;
    private final ImageService imageService;
    private final MessageSource messageSource;

    public PhotoEvolutionService(PhotoEvolutionRepository repository, PhotoEvolutionMapper mapper,
                                 UserRepository userRepository, ImageService imageService, MessageSource messageSource) {
        this.repository = repository;
        this.mapper = mapper;
        this.userRepository = userRepository;
        this.imageService = imageService;
        this.messageSource = messageSource;
    }

    @Transactional
    public PhotoEvolutionDTO create(PhotoEvolutionDTO dto) {
        logger.debug("Creating photo evolution entry");
        if (dto == null || dto.getBodyPart() == null) {
            throw invalidBodyException();
        }
        Users owner = resolveOwnerForMutation(dto.getUserId());
        Image image = resolveImage(dto.getImageId(), owner.getId());

        PhotoEvolution entity = mapper.toEntity(dto);
        entity.setUser(owner);
        entity.setImage(image);

        PhotoEvolution saved = repository.save(entity);
        logger.debug("Photo evolution {} created for user {}", saved.getId(), owner.getId());
        return mapper.toDTO(saved);
    }

    @Transactional
    public PhotoEvolutionDTO update(UUID id, PhotoEvolutionDTO dto) {
        logger.debug("Updating photo evolution entry {}", id);
        if (id == null || dto == null) {
            throw invalidBodyException();
        }
        PhotoEvolution entity = repository.findById(id).orElseThrow(this::photoEvolutionNotFound);
        enforceOwnership(entity);

        if (dto.getBodyPart() != null) {
            entity.setBodyPart(dto.getBodyPart());
        }
        mapper.updateEntityFromDto(dto, entity);

        if (dto.getImageId() != null
                && (entity.getImage() == null || !dto.getImageId().equals(entity.getImage().getId()))) {
            Image image = resolveImage(dto.getImageId(), entity.getUser().getId());
            entity.setImage(image);
        }

        if (dto.getCapturedAt() != null) {
            entity.setCapturedAt(dto.getCapturedAt());
        }

        PhotoEvolution saved = repository.save(entity);
        logger.debug("Photo evolution {} updated", saved.getId());
        return mapper.toDTO(saved);
    }

    @Transactional(readOnly = true)
    public PhotoEvolutionDTO findById(UUID id) {
        if (id == null) {
            throw invalidBodyException();
        }
        PhotoEvolution entity = repository.findById(id).orElseThrow(this::photoEvolutionNotFound);
        enforceOwnership(entity);
        return mapper.toDTO(entity);
    }

    @Transactional(readOnly = true)
    public List<PhotoEvolutionDTO> list(UUID userId, BodyPart bodyPart) {
        UUID targetUserId = resolveUserForQuery(userId);
        List<PhotoEvolution> results;

        if (targetUserId != null) {
            if (bodyPart != null) {
                results = repository.findByUser_IdAndBodyPartOrderByCapturedAtDesc(targetUserId, bodyPart);
            } else {
                results = repository.findByUser_IdOrderByCapturedAtDesc(targetUserId);
            }
        } else if (bodyPart != null) {
            results = repository.findByBodyPartOrderByCapturedAtDesc(bodyPart);
        } else {
            results = repository.findAllByOrderByCapturedAtDesc();
        }

        return results.stream().map(mapper::toDTO).collect(Collectors.toList());
    }

    @Transactional
    public void delete(UUID id) {
        if (id == null) {
            throw invalidBodyException();
        }
        PhotoEvolution entity = repository.findById(id).orElseThrow(this::photoEvolutionNotFound);
        enforceOwnership(entity);
        repository.delete(entity);
        logger.debug("Photo evolution {} deleted", id);
    }

    @Transactional(readOnly = true)
    public List<PhotoEvolutionOwnerDTO> listOwners(String query) {
        if (!SecurityUtils.hasRole("ADMIN")) {
            UUID currentUserId = SecurityUtils.getCurrentUserId().orElseThrow(this::photoEvolutionForbidden);
            Users currentUser = userRepository.findById(currentUserId).orElseThrow(this::userNotFound);
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

    private Users resolveOwnerForMutation(UUID requestedUserId) {
        boolean admin = SecurityUtils.hasRole("ADMIN");
        UUID currentUserId = SecurityUtils.getCurrentUserId().orElseThrow(this::photoEvolutionForbidden);

        UUID ownerId;
        if (admin) {
            ownerId = Optional.ofNullable(requestedUserId).orElse(currentUserId);
        } else {
            if (requestedUserId != null && !requestedUserId.equals(currentUserId)) {
                throw photoEvolutionForbidden();
            }
            ownerId = currentUserId;
        }
        return userRepository.findById(ownerId).orElseThrow(this::userNotFound);
    }

    private UUID resolveUserForQuery(UUID requestedUserId) {
        boolean admin = SecurityUtils.hasRole("ADMIN");
        if (admin) {
            return requestedUserId;
        }
        UUID currentUserId = SecurityUtils.getCurrentUserId().orElseThrow(this::photoEvolutionForbidden);
        if (requestedUserId != null && !requestedUserId.equals(currentUserId)) {
            throw photoEvolutionForbidden();
        }
        return currentUserId;
    }

    private void enforceOwnership(PhotoEvolution entity) {
        if (entity == null) {
            return;
        }
        if (SecurityUtils.hasRole("ADMIN")) {
            return;
        }
        UUID currentUserId = SecurityUtils.getCurrentUserId().orElseThrow(this::photoEvolutionForbidden);
        if (!entity.getUser().getId().equals(currentUserId)) {
            throw photoEvolutionForbidden();
        }
    }

    private Image resolveImage(UUID imageId, UUID expectedUserId) {
        if (imageId == null) {
            throw invalidImage();
        }
        Image image = imageService.findEntityById(imageId);
        if (image.getUsers() != null && expectedUserId != null && !expectedUserId.equals(image.getUsers().getId())) {
            throw invalidImage();
        }
        return image;
    }

    private JMException invalidBodyException() {
        ProblemType type = ProblemType.INVALID_BODY;
        Locale locale = LocaleContextHolder.getLocale();
        String message = messageSource.getMessage(type.getMessageSource(), new Object[]{""}, locale);
        return new JMException(HttpStatus.BAD_REQUEST.value(), type.getTitle(), type.getUri(), message);
    }

    private JMException photoEvolutionNotFound() {
        ProblemType type = ProblemType.PHOTO_EVOLUTION_NOT_FOUND;
        Locale locale = LocaleContextHolder.getLocale();
        String message = messageSource.getMessage(type.getMessageSource(), new Object[]{""}, locale);
        return new JMException(HttpStatus.NOT_FOUND.value(), type.getTitle(), type.getUri(), message);
    }

    private JMException photoEvolutionForbidden() {
        ProblemType type = ProblemType.PHOTO_EVOLUTION_FORBIDDEN;
        Locale locale = LocaleContextHolder.getLocale();
        String message = messageSource.getMessage(type.getMessageSource(), new Object[]{""}, locale);
        return new JMException(HttpStatus.FORBIDDEN.value(), type.getTitle(), type.getUri(), message);
    }

    private JMException userNotFound() {
        ProblemType type = ProblemType.USER_NOT_FOUND;
        Locale locale = LocaleContextHolder.getLocale();
        String message = messageSource.getMessage(type.getMessageSource(), new Object[]{""}, locale);
        return new JMException(HttpStatus.BAD_REQUEST.value(), type.getTitle(), type.getUri(), message);
    }

    private JMException invalidImage() {
        ProblemType type = ProblemType.PHOTO_EVOLUTION_INVALID_IMAGE;
        Locale locale = LocaleContextHolder.getLocale();
        String message = messageSource.getMessage(type.getMessageSource(), new Object[]{""}, locale);
        return new JMException(HttpStatus.BAD_REQUEST.value(), type.getTitle(), type.getUri(), message);
    }

    private PhotoEvolutionOwnerDTO toOwnerDto(Users user) {
        return PhotoEvolutionOwnerDTO.builder()
                .id(user.getId())
                .displayName(mapperDisplayName(user))
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }

    private String mapperDisplayName(Users user) {
        String name = user.getName();
        String lastName = user.getLastName();
        if (!StringUtils.hasText(name)) {
            return lastName;
        }
        if (!StringUtils.hasText(lastName)) {
            return name;
        }
        return name + " " + lastName;
    }
}
