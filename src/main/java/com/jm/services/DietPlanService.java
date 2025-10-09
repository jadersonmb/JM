package com.jm.services;

import com.jm.dto.DietMealDTO;
import com.jm.dto.DietMealItemDTO;
import com.jm.dto.DietPlanDTO;
import com.jm.entity.DietMeal;
import com.jm.entity.DietMealItem;
import com.jm.entity.DietPlan;
import com.jm.entity.FoodItem;
import com.jm.entity.UnitOfMeasure;
import com.jm.execption.JMException;
import com.jm.execption.ProblemType;
import com.jm.mappers.DietPlanMapper;
import com.jm.repository.DietPlanRepository;
import com.jm.repository.FoodItemRepository;
import com.jm.repository.UnitOfMeasureRepository;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

@Service
public class DietPlanService {

    private static final Logger logger = LoggerFactory.getLogger(DietPlanService.class);

    private final DietPlanRepository dietPlanRepository;
    private final DietPlanMapper dietPlanMapper;
    private final MessageSource messageSource;
    private final FoodItemRepository foodItemRepository;
    private final UnitOfMeasureRepository unitOfMeasureRepository;

    public DietPlanService(DietPlanRepository dietPlanRepository, DietPlanMapper dietPlanMapper,
            MessageSource messageSource, FoodItemRepository foodItemRepository,
            UnitOfMeasureRepository unitOfMeasureRepository) {
        this.dietPlanRepository = dietPlanRepository;
        this.dietPlanMapper = dietPlanMapper;
        this.messageSource = messageSource;
        this.foodItemRepository = foodItemRepository;
        this.unitOfMeasureRepository = unitOfMeasureRepository;
    }

    @Transactional(readOnly = true)
    public Page<DietPlanDTO> findAll(Pageable pageable, DietPlanDTO filter) {
        DietPlanDTO criteria = filter != null ? filter : new DietPlanDTO();
        if (isClient()) {
            SecurityUtils.getCurrentUserId().ifPresent(criteria::setCreatedByUserId);
        }
        Page<DietPlan> result = dietPlanRepository.findAll(DietPlanSpecification.search(criteria), pageable);
        return result.map(dietPlanMapper::toDTO);
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
        return dietPlanMapper.toDTO(saved);
    }

    @Transactional(readOnly = true)
    public DietPlanDTO findById(UUID id) {
        DietPlan entity = dietPlanRepository.findById(id).orElseThrow(this::dietNotFound);
        enforceOwnership(entity);
        return dietPlanMapper.toDTO(entity);
    }

    @Transactional
    public void delete(UUID id) {
        DietPlan entity = dietPlanRepository.findById(id).orElseThrow(this::dietNotFound);
        enforceOwnership(entity);
        dietPlanRepository.delete(entity);
        logger.debug("{} - {}", getMessage("diet.deleted"), id);
    }

    private void applyMeals(DietPlan entity, List<DietMealDTO> meals) {
        entity.getMeals().clear();
        if (CollectionUtils.isEmpty(meals)) {
            throw invalidBodyException();
        }

        for (DietMealDTO mealDTO : meals) {
            if (mealDTO == null || mealDTO.getMealType() == null) {
                throw invalidBodyException();
            }

            DietMeal meal = dietPlanMapper.toMealEntity(mealDTO);
            meal.setId(mealDTO.getId());
            meal.setDietPlan(entity);
            meal.getItems().clear();
            applyMealItems(meal, mealDTO.getItems());
            entity.getMeals().add(meal);
        }
    }

    private void applyMealItems(DietMeal meal, List<DietMealItemDTO> items) {
        if (items == null) {
            return;
        }

        for (DietMealItemDTO itemDTO : items) {
            validateMealItem(itemDTO);

            DietMealItem item = dietPlanMapper.toMealItemEntity(itemDTO);
            item.setId(itemDTO.getId());
            item.setMeal(meal);
            item.setFoodItem(resolveFoodItem(itemDTO.getFoodItemId()));
            item.setUnit(resolveUnit(itemDTO.getUnitId()));
            meal.getItems().add(item);
        }
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

    private FoodItem resolveFoodItem(UUID id) {
        if (id == null) {
            throw foodNotFound();
        }
        return foodItemRepository.findById(id).orElseThrow(this::foodNotFound);
    }

    private UnitOfMeasure resolveUnit(UUID id) {
        if (id == null) {
            throw unitNotFound();
        }
        return unitOfMeasureRepository.findById(id).orElseThrow(this::unitNotFound);
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

