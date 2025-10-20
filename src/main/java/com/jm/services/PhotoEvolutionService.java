package com.jm.services;

import com.jm.dto.PhotoEvolutionBodyPartComparisonDTO;
import com.jm.dto.PhotoEvolutionComparisonDTO;
import com.jm.dto.PhotoEvolutionCreateRequest;
import com.jm.dto.PhotoEvolutionDTO;
import com.jm.dto.PhotoEvolutionEvolutionPointDTO;
import com.jm.dto.PhotoEvolutionOwnerDTO;
import com.jm.dto.PhotoEvolutionPrefillDTO;
import com.jm.dto.PhotoEvolutionUpdateRequest;
import com.jm.entity.Image;
import com.jm.entity.PhotoEvolution;
import com.jm.entity.Users;
import com.jm.enums.BodyPart;
import com.jm.execption.JMException;
import com.jm.execption.ProblemType;
import com.jm.mappers.ImageMapper;
import com.jm.mappers.PhotoEvolutionComparisonMapper;
import com.jm.mappers.PhotoEvolutionMapper;
import com.jm.repository.AnamnesisRepository;
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
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PhotoEvolutionService {

    private static final Logger logger = LoggerFactory.getLogger(PhotoEvolutionService.class);

    private final PhotoEvolutionRepository repository;
    private final PhotoEvolutionMapper mapper;
    private final PhotoEvolutionComparisonMapper comparisonMapper;
    private final UserRepository userRepository;
    private final CloudflareR2Service cloudflareR2Service;
    private final AnamnesisRepository anamnesisRepository;
    private final MessageSource messageSource;
    private final ImageMapper imageMapper;

    public PhotoEvolutionService(PhotoEvolutionRepository repository, PhotoEvolutionMapper mapper,
            PhotoEvolutionComparisonMapper comparisonMapper,
            UserRepository userRepository,
            CloudflareR2Service cloudflareR2Service, AnamnesisRepository anamnesisRepository,
            MessageSource messageSource, ImageMapper imageMapper) {
        this.repository = repository;
        this.mapper = mapper;
        this.comparisonMapper = comparisonMapper;
        this.userRepository = userRepository;
        this.cloudflareR2Service = cloudflareR2Service;
        this.anamnesisRepository = anamnesisRepository;
        this.messageSource = messageSource;
        this.imageMapper = imageMapper;
    }

    @Transactional
    public List<PhotoEvolutionDTO> create(List<PhotoEvolutionCreateRequest> requests, List<MultipartFile> images) {
        logger.debug("Creating photo evolution entries");
        if (requests == null || requests.isEmpty()) {
            throw invalidBodyException();
        }
        if (images == null || images.isEmpty()) {
            throw invalidImage();
        }
        if (requests.size() != images.size()) {
            throw invalidImage();
        }

        List<PhotoEvolutionDTO> results = new ArrayList<>();
        for (int i = 0; i < requests.size(); i++) {
            PhotoEvolutionCreateRequest request = requests.get(i);
            MultipartFile file = images.get(i);
            if (request == null || request.getBodyPart() == null) {
                throw invalidBodyException();
            }
            if (file == null || file.isEmpty()) {
                throw invalidImage();
            }
            Users owner = resolveOwnerForMutation(request.getUserId());
            Image image = uploadImageForUser(file, owner.getId());

            PhotoEvolution entity = new PhotoEvolution();
            entity.setUser(owner);
            entity.setImage(image);
            mapper.updateEntityFromCreateRequest(request, entity);

            PhotoEvolution saved = repository.save(entity);
            logger.debug("Photo evolution {} created for user {}", saved.getId(), owner.getId());
            results.add(mapper.toDTO(saved));
        }

        return results;
    }

    @Transactional
    public PhotoEvolutionDTO update(UUID id, PhotoEvolutionUpdateRequest request, MultipartFile imageFile) {
        logger.debug("Updating photo evolution entry {}", id);
        if (id == null || request == null) {
            throw invalidBodyException();
        }
        PhotoEvolution entity = repository.findById(id).orElseThrow(this::photoEvolutionNotFound);
        enforceOwnership(entity);

        mapper.updateEntityFromUpdateRequest(request, entity);

        Image previousImage = entity.getImage();
        Image newImage = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            newImage = uploadImageForUser(imageFile, entity.getUser().getId());
            entity.setImage(newImage);
        }
        PhotoEvolution saved = repository.save(entity);
        if (newImage != null && previousImage != null && previousImage.getId() != null
                && !previousImage.getId().equals(newImage.getId())) {
            deleteImage(previousImage, entity.getUser().getId());
        }
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

    @Transactional(readOnly = true)
    public PhotoEvolutionPrefillDTO prefill(UUID userId) {
        UUID targetUserId = resolveUserForQuery(userId);
        if (targetUserId == null) {
            targetUserId = SecurityUtils.getCurrentUserId().orElseThrow(this::photoEvolutionForbidden);
        }

        return anamnesisRepository.findTopByUserIdOrderByIdDesc(targetUserId)
                .map(anamnesis -> PhotoEvolutionPrefillDTO.builder()
                        .weight(anamnesis.getWeightKg())
                        .bodyFatPercentage(anamnesis.getBodyFatPercentage())
                        .muscleMass(anamnesis.getMuscleMassPercentage())
                        .waistCircumference(anamnesis.getWaistCircumference())
                        .hipCircumference(anamnesis.getHipCircumference())
                        .chestCircumference(anamnesis.getThoraxCircumference())
                        .leftArmCircumference(anamnesis.getArmCircumference())
                        .rightArmCircumference(anamnesis.getArmCircumference())
                        .leftThighCircumference(anamnesis.getKneeCircumference())
                        .rightThighCircumference(anamnesis.getKneeCircumference())
                        .build())
                .orElseGet(() -> PhotoEvolutionPrefillDTO.builder().build());
    }

    @Transactional(readOnly = true)
    public PhotoEvolutionComparisonDTO getComparisonByUser(UUID userId) {
        UUID targetUserId = resolveUserForQuery(userId);
        if (targetUserId == null) {
            targetUserId = SecurityUtils.getCurrentUserId().orElseThrow(this::photoEvolutionForbidden);
        }

        List<PhotoEvolution> entries = repository.findByUser_IdOrderByCapturedAtDesc(targetUserId);
        if (entries.isEmpty()) {
            throw photoEvolutionComparisonNotFound();
        }

        Map<BodyPart, List<PhotoEvolution>> grouped = entries.stream()
                .filter(entry -> entry.getBodyPart() != null)
                .collect(Collectors.groupingBy(PhotoEvolution::getBodyPart));

        if (grouped.isEmpty()) {
            throw photoEvolutionComparisonNotFound();
        }

        Comparator<PhotoEvolution> byCaptureDate = Comparator
                .comparing(PhotoEvolution::getCapturedAt, Comparator.nullsLast(LocalDate::compareTo))
                .thenComparing(PhotoEvolution::getCreatedAt, Comparator.nullsLast(LocalDateTime::compareTo));

        List<PhotoEvolutionBodyPartComparisonDTO> parts = grouped.entrySet().stream()
                .map(entry -> buildComparison(entry.getKey(), entry.getValue(), byCaptureDate))
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(PhotoEvolutionBodyPartComparisonDTO::getBodyPart))
                .collect(Collectors.toList());

        if (parts.isEmpty()) {
            throw photoEvolutionComparisonNotFound();
        }

        return PhotoEvolutionComparisonDTO.builder()
                .userId(targetUserId)
                .parts(parts)
                .build();
    }

    private PhotoEvolutionBodyPartComparisonDTO buildComparison(BodyPart bodyPart, List<PhotoEvolution> items,
            Comparator<PhotoEvolution> comparator) {
        if (items == null || items.isEmpty()) {
            return null;
        }

        List<PhotoEvolution> ordered = new ArrayList<>(items);
        ordered.sort(comparator);

        List<PhotoEvolutionEvolutionPointDTO> evolution = comparisonMapper.toPoints(ordered);

        Double initialMeasurement = evolution.stream()
                .map(PhotoEvolutionEvolutionPointDTO::getMeasurement)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);

        Double currentMeasurement = null;
        for (int i = evolution.size() - 1; i >= 0; i--) {
            Double candidate = evolution.get(i).getMeasurement();
            if (candidate != null) {
                currentMeasurement = candidate;
                break;
            }
        }

        Double variation = calculateVariation(initialMeasurement, currentMeasurement);

        Comparator<PhotoEvolution> reversedComparator = comparator.reversed();
        String latestImage = ordered.stream()
                .sorted(reversedComparator)
                .map(PhotoEvolution::getImage)
                .filter(Objects::nonNull)
                .map(Image::getUrl)
                .filter(StringUtils::hasText)
                .findFirst()
                .orElse(null);

        return PhotoEvolutionBodyPartComparisonDTO.builder()
                .bodyPartCode(bodyPart.name())
                .bodyPart(formatBodyPart(bodyPart))
                .latestImage(latestImage)
                .initialMeasurement(initialMeasurement)
                .currentMeasurement(currentMeasurement)
                .variation(variation)
                .evolution(evolution)
                .build();
    }

    private Double calculateVariation(Double initialMeasurement, Double currentMeasurement) {
        if (initialMeasurement == null || currentMeasurement == null) {
            return 0.0;
        }
        if (Math.abs(initialMeasurement) < 1e-6) {
            return 0.0;
        }
        return ((currentMeasurement - initialMeasurement) / initialMeasurement) * 100.0;
    }

    private String formatBodyPart(BodyPart bodyPart) {
        String name = bodyPart.name().toLowerCase(Locale.ROOT).replace('_', ' ');
        String[] parts = name.split(" ");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                if (builder.length() > 0) {
                    builder.append(' ');
                }
                builder.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
            }
        }
        return builder.toString();
    }

    @Transactional
    public void delete(UUID id) {
        if (id == null) {
            throw invalidBodyException();
        }
        PhotoEvolution entity = repository.findById(id).orElseThrow(this::photoEvolutionNotFound);
        enforceOwnership(entity);
        Image image = entity.getImage();
        UUID userId = entity.getUser() != null ? entity.getUser().getId() : null;
        repository.delete(entity);
        deleteImage(image, userId);
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

    private Image uploadImageForUser(MultipartFile file, UUID userId) {
        try {
            return imageMapper.toEntity(cloudflareR2Service.uploadImage(file, userId));
        } catch (Exception ex) {
            logger.debug("Failed to upload image for user {}: {}", userId, ex.getMessage());
            throw invalidImage();
        }
    }

    private void deleteImage(Image image, UUID userId) {
        if (image == null || userId == null) {
            return;
        }
        if (!StringUtils.hasText(image.getFileName())) {
            return;
        }
        cloudflareR2Service.deleteFileByGeneratedName(userId, image.getFileName());
    }

    private JMException invalidBodyException() {
        ProblemType type = ProblemType.INVALID_BODY;
        Locale locale = LocaleContextHolder.getLocale();
        String message = messageSource.getMessage(type.getMessageSource(), new Object[] { "" }, locale);
        return new JMException(HttpStatus.BAD_REQUEST.value(), type.getTitle(), type.getUri(), message);
    }

    private JMException photoEvolutionNotFound() {
        ProblemType type = ProblemType.PHOTO_EVOLUTION_NOT_FOUND;
        Locale locale = LocaleContextHolder.getLocale();
        String message = messageSource.getMessage(type.getMessageSource(), new Object[] { "" }, locale);
        return new JMException(HttpStatus.NOT_FOUND.value(), type.getTitle(), type.getUri(), message);
    }

    private JMException photoEvolutionForbidden() {
        ProblemType type = ProblemType.PHOTO_EVOLUTION_FORBIDDEN;
        Locale locale = LocaleContextHolder.getLocale();
        String message = messageSource.getMessage(type.getMessageSource(), new Object[] { "" }, locale);
        return new JMException(HttpStatus.FORBIDDEN.value(), type.getTitle(), type.getUri(), message);
    }

    private JMException photoEvolutionComparisonNotFound() {
        ProblemType type = ProblemType.PHOTO_EVOLUTION_NOT_FOUND;
        Locale locale = LocaleContextHolder.getLocale();
        String message = messageSource.getMessage("photoevolution.data.notfound", null, locale);
        return new JMException(HttpStatus.NOT_FOUND.value(), type.getTitle(), type.getUri(), message);
    }

    private JMException userNotFound() {
        ProblemType type = ProblemType.USER_NOT_FOUND;
        Locale locale = LocaleContextHolder.getLocale();
        String message = messageSource.getMessage(type.getMessageSource(), new Object[] { "" }, locale);
        return new JMException(HttpStatus.BAD_REQUEST.value(), type.getTitle(), type.getUri(), message);
    }

    private JMException invalidImage() {
        ProblemType type = ProblemType.PHOTO_EVOLUTION_INVALID_IMAGE;
        Locale locale = LocaleContextHolder.getLocale();
        String message = messageSource.getMessage(type.getMessageSource(), new Object[] { "" }, locale);
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
