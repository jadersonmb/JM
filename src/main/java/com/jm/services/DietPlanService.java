package com.jm.services;

import com.jm.dto.DietMealDTO;
import com.jm.dto.DietMealItemDTO;
import com.jm.dto.DietOwnerDTO;
import com.jm.dto.DietPlanDTO;
import com.jm.entity.DietMeal;
import com.jm.entity.DietMealItem;
import com.jm.entity.DietPlan;
import com.jm.entity.Food;
import com.jm.entity.MeasurementUnits;
import com.jm.entity.Users;
import com.jm.execption.JMException;
import com.jm.execption.ProblemType;
import com.jm.mappers.DietPlanMapper;
import com.jm.repository.DietPlanRepository;
import com.jm.repository.FoodRepository;
import com.jm.repository.MeasurementUnitRepository;
import com.jm.repository.UserRepository;
import com.jm.speciation.DietPlanSpecification;
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
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DietPlanService {

    private static final Logger logger = LoggerFactory.getLogger(DietPlanService.class);

    private final DietPlanRepository dietPlanRepository;
    private final DietPlanMapper dietPlanMapper;
    private final MessageSource messageSource;
    private final FoodRepository foodRepository;
    private final MeasurementUnitRepository measurementUnitRepository;
    private final UserRepository userRepository;

    public DietPlanService(DietPlanRepository dietPlanRepository, DietPlanMapper dietPlanMapper,
            MessageSource messageSource, FoodRepository foodRepository,
            MeasurementUnitRepository measurementUnitRepository, UserRepository userRepository) {
        this.dietPlanRepository = dietPlanRepository;
        this.dietPlanMapper = dietPlanMapper;
        this.messageSource = messageSource;
        this.foodRepository = foodRepository;
        this.measurementUnitRepository = measurementUnitRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public Page<DietPlanDTO> findAll(Pageable pageable, DietPlanDTO filter) {
        DietPlanDTO criteria = filter != null ? filter : new DietPlanDTO();
        if (isClient()) {
            SecurityUtils.getCurrentUserId().ifPresent(criteria::setCreatedByUserId);
        }
        Page<DietPlan> result = dietPlanRepository.findAll(DietPlanSpecification.search(criteria), pageable);
        Page<DietPlanDTO> pageResult = result.map(dietPlanMapper::toDTO);
        populateOwnerDetails(pageResult.getContent());
        pageResult.forEach(this::sortMealsAndItems);
        return pageResult;
    }

    @Transactional
    public DietPlanDTO create(DietPlanDTO dto) {
        if (dto == null) {
            throw invalidBodyException();
        }

        DietPlan entity;
        boolean update = dto.getId() != null;
        if (update) {
            entity = dietPlanRepository.findById(dto.getId()).orElseThrow(this::dietNotFound);
            enforceOwnership(entity);
        } else {
            entity = new DietPlan();
            UUID ownerId = resolveOwner(dto);
            entity.setCreatedByUserId(ownerId);
        }

        if (isAdmin() && dto.getCreatedByUserId() != null) {
            entity.setCreatedByUserId(dto.getCreatedByUserId());
        }

        entity.setPatientName(dto.getPatientName());
        entity.setNotes(dto.getNotes());
        entity.setActive(dto.getActive() != null ? dto.getActive() : Boolean.TRUE);

        applyMeals(entity, dto.getMeals());

        DietPlan saved = dietPlanRepository.save(entity);
        logger.debug("{} - {}", getMessage("diet.saved"), saved.getId());
        return toDetailedDto(saved);
    }

    @Transactional(readOnly = true)
    public DietPlanDTO findById(UUID id) {
        DietPlan entity = dietPlanRepository.findById(id).orElseThrow(this::dietNotFound);
        enforceOwnership(entity);
        return toDetailedDto(entity);
    }

    @Transactional
    public void delete(UUID id) {
        DietPlan entity = dietPlanRepository.findById(id).orElseThrow(this::dietNotFound);
        enforceOwnership(entity);
        dietPlanRepository.delete(entity);
        logger.debug("{} - {}", getMessage("diet.deleted"), id);
    }

    @Transactional(readOnly = true)
    public List<DietOwnerDTO> listOwners(String query) {
        if (isClient()) {
            UUID currentUserId = SecurityUtils.getCurrentUserId().orElseThrow(this::invalidBodyException);
            Users currentUser = userRepository.findById(currentUserId).orElseThrow(this::dietForbidden);
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

        return users.stream()
                .map(this::toOwnerDto)
                .collect(Collectors.toList());
    }

    private DietOwnerDTO toOwnerDto(Users user) {
        if (user == null) {
            return null;
        }
        return DietOwnerDTO.builder()
                .id(user.getId())
                .displayName(formatUserName(user))
                .email(user.getEmail())
                .build();
    }

    private void applyMeals(DietPlan entity, List<DietMealDTO> meals) {
        if (CollectionUtils.isEmpty(meals)) {
            throw invalidBodyException();
        }

        Map<UUID, DietMeal> existingMeals = entity.getMeals().stream()
                .filter(meal -> meal.getId() != null)
                .collect(Collectors.toMap(DietMeal::getId, meal -> meal, (left, right) -> left, LinkedHashMap::new));

        List<DietMeal> updatedMeals = new ArrayList<>();
        for (DietMealDTO mealDTO : meals) {
            if (mealDTO == null || mealDTO.getMealType() == null) {
                throw invalidBodyException();
            }

            DietMeal meal = resolveExistingMeal(existingMeals, mealDTO.getId());
            meal.setDietPlan(entity);
            meal.setMealType(mealDTO.getMealType());
            meal.setScheduledTime(mealDTO.getScheduledTime());
            applyMealItems(meal, mealDTO.getItems());
            updatedMeals.add(meal);
        }

        entity.getMeals().clear();
        entity.getMeals().addAll(updatedMeals);
    }

    private DietMeal resolveExistingMeal(Map<UUID, DietMeal> existingMeals, UUID id) {
        if (id == null || existingMeals.isEmpty()) {
            return new DietMeal();
        }
        DietMeal meal = existingMeals.remove(id);
        if (meal == null) {
            throw invalidReference();
        }
        return meal;
    }

    private void applyMealItems(DietMeal meal, List<DietMealItemDTO> items) {
        Map<UUID, DietMealItem> existingItems = meal.getItems().stream()
                .filter(item -> item.getId() != null)
                .collect(
                        Collectors.toMap(DietMealItem::getId, item -> item, (left, right) -> left, LinkedHashMap::new));

        List<DietMealItem> updatedItems = new ArrayList<>();
        if (items != null) {
            for (DietMealItemDTO itemDTO : items) {
                validateMealItem(itemDTO);
                DietMealItem item = resolveExistingMealItem(existingItems, itemDTO.getId());
                item.setMeal(meal);
                item.setFoodItem(resolveFood(itemDTO.getFoodItemId()));
                item.setUnit(resolveMeasurementUnit(itemDTO.getUnitId()));
                item.setQuantity(itemDTO.getQuantity());
                item.setNotes(StringUtils.hasText(itemDTO.getNotes()) ? itemDTO.getNotes() : null);
                updatedItems.add(item);
            }
        }

        meal.getItems().clear();
        meal.getItems().addAll(updatedItems);
    }

    private DietMealItem resolveExistingMealItem(Map<UUID, DietMealItem> existingItems, UUID id) {
        if (id == null || existingItems.isEmpty()) {
            return new DietMealItem();
        }
        DietMealItem item = existingItems.remove(id);
        if (item == null) {
            throw invalidReference();
        }
        return item;
    }

    private void validateMealItem(DietMealItemDTO itemDTO) {
        if (itemDTO == null) {
            throw invalidBodyException();
        }

        if (itemDTO.getFoodItemId() == null) {
            throw foodNotFound();
        }
        if (itemDTO.getUnitId() == null) {
            throw unitNotFound();
        }
        BigDecimal quantity = itemDTO.getQuantity();
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw invalidReference();
        }
    }

    private Food resolveFood(UUID id) {
        if (id == null) {
            throw foodNotFound();
        }
        return foodRepository.findById(id).orElseThrow(this::foodNotFound);
    }

    private MeasurementUnits resolveMeasurementUnit(UUID id) {
        if (id == null) {
            throw unitNotFound();
        }
        return measurementUnitRepository.findById(id).orElseThrow(this::unitNotFound);
    }

    private UUID resolveOwner(DietPlanDTO dto) {
        if (dto.getCreatedByUserId() != null) {
            return dto.getCreatedByUserId();
        }
        return SecurityUtils.getCurrentUserId().orElseThrow(this::invalidBodyException);
    }

    private void enforceOwnership(DietPlan entity) {
        if (!isClient()) {
            return;
        }
        UUID currentUserId = SecurityUtils.getCurrentUserId().orElse(null);
        if (currentUserId == null || !Objects.equals(currentUserId, entity.getCreatedByUserId())) {
            throw dietForbidden();
        }
    }

    private DietPlanDTO toDetailedDto(DietPlan entity) {
        DietPlanDTO dto = dietPlanMapper.toDTO(entity);
        populateOwnerDetails(List.of(dto));
        sortMealsAndItems(dto);
        return dto;
    }

    private void populateOwnerDetails(Collection<DietPlanDTO> dtos) {
        if (CollectionUtils.isEmpty(dtos)) {
            return;
        }

        Set<UUID> ownerIds = dtos.stream()
                .map(DietPlanDTO::getCreatedByUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (ownerIds.isEmpty()) {
            return;
        }

        Map<UUID, Users> owners = userRepository.findAllById(ownerIds).stream()
                .collect(Collectors.toMap(Users::getId, user -> user));

        for (DietPlanDTO dto : dtos) {
            Users owner = owners.get(dto.getCreatedByUserId());
            if (owner != null) {
                dto.setCreatedByName(formatUserName(owner));
                dto.setCreatedByEmail(owner.getEmail());
            }
        }
    }

    private void sortMealsAndItems(DietPlanDTO dto) {
        if (dto == null || CollectionUtils.isEmpty(dto.getMeals())) {
            return;
        }

        dto.getMeals().sort(Comparator
                .comparing(DietMealDTO::getScheduledTime, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(DietMealDTO::getMealType, Comparator.nullsLast(Comparator.naturalOrder())));

        dto.getMeals().forEach(meal -> {
            if (CollectionUtils.isEmpty(meal.getItems())) {
                return;
            }
            meal.getItems().sort(Comparator
                    .comparing(DietMealItemDTO::getFoodItemName, Comparator.nullsLast(String::compareToIgnoreCase))
                    .thenComparing(DietMealItemDTO::getQuantity, Comparator.nullsLast(Comparator.naturalOrder())));
        });
    }

    private String formatUserName(Users user) {
        if (user == null) {
            return null;
        }
        String firstName = StringUtils.hasText(user.getName()) ? user.getName().trim() : "";
        String lastName = StringUtils.hasText(user.getLastName()) ? user.getLastName().trim() : "";
        String combined = (firstName + " " + lastName).trim();
        if (StringUtils.hasText(combined)) {
            return combined;
        }
        return user.getEmail();
    }

    private JMException dietNotFound() {
        return buildException(ProblemType.DIET_NOT_FOUND, HttpStatus.BAD_REQUEST);
    }

    private JMException dietForbidden() {
        return buildException(ProblemType.DIET_FORBIDDEN, HttpStatus.FORBIDDEN);
    }

    private JMException invalidReference() {
        return buildException(ProblemType.DIET_INVALID_REFERENCE, HttpStatus.BAD_REQUEST);
    }

    private JMException unitNotFound() {
        return buildException(ProblemType.DIET_UNIT_NOT_FOUND, HttpStatus.BAD_REQUEST);
    }

    private JMException foodNotFound() {
        return buildException(ProblemType.DIET_FOOD_NOT_FOUND, HttpStatus.BAD_REQUEST);
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

    private boolean isAdmin() {
        return SecurityUtils.hasRole("ADMIN");
    }
}
