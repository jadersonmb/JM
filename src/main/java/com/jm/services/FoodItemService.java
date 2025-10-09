package com.jm.services;

import com.jm.dto.FoodItemDTO;
import com.jm.entity.FoodItem;
import com.jm.execption.JMException;
import com.jm.execption.ProblemType;
import com.jm.mappers.DietPlanMapper;
import com.jm.repository.FoodItemRepository;
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
public class FoodItemService {

    private static final Logger logger = LoggerFactory.getLogger(FoodItemService.class);

    private final FoodItemRepository repository;
    private final DietPlanMapper mapper;
    private final MessageSource messageSource;

    public FoodItemService(FoodItemRepository repository, DietPlanMapper mapper, MessageSource messageSource) {
        this.repository = repository;
        this.mapper = mapper;
        this.messageSource = messageSource;
    }

    public List<FoodItemDTO> findAll(Boolean onlyActive) {
        List<FoodItem> foods;
        if (Boolean.TRUE.equals(onlyActive)) {
            foods = repository.findByActiveTrueOrderByNameAsc();
        } else {
            foods = repository.findAll(Sort.by(Sort.Direction.ASC, "name"));
        }
        logger.debug("Loaded {} food items", foods.size());
        return foods.stream().map(mapper::toFoodItemDTO).collect(Collectors.toList());
    }

    public FoodItemDTO findById(UUID id) {
        return mapper.toFoodItemDTO(repository.findById(id).orElseThrow(this::foodNotFound));
    }

    private JMException foodNotFound() {
        Locale locale = LocaleContextHolder.getLocale();
        String messageDetails = messageSource.getMessage(ProblemType.DIET_FOOD_NOT_FOUND.getMessageSource(),
                new Object[] { "" }, locale);
        return new JMException(HttpStatus.BAD_REQUEST.value(), ProblemType.DIET_FOOD_NOT_FOUND.getTitle(),
                ProblemType.DIET_FOOD_NOT_FOUND.getUri(), messageDetails);
    }
}
