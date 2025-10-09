package com.jm.services;

import com.jm.dto.FoodItemDTO;
import com.jm.entity.Food;
import com.jm.execption.JMException;
import com.jm.execption.ProblemType;
import com.jm.repository.FoodRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FoodItemService {

    private static final Logger logger = LoggerFactory.getLogger(FoodItemService.class);

    private final FoodRepository repository;
    private final MessageSource messageSource;

    public FoodItemService(FoodRepository repository, MessageSource messageSource) {
        this.repository = repository;
        this.messageSource = messageSource;
    }

    @Transactional(readOnly = true)
    public List<FoodItemDTO> findAll(Boolean onlyActive) {
        List<Food> foods;
        if (Boolean.TRUE.equals(onlyActive)) {
            foods = repository.findByIsActiveTrueOrderByNameAsc();
        } else {
            foods = repository.findAllByOrderByNameAsc();
        }
        logger.debug("Loaded {} foods", foods.size());
        return foods.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public FoodItemDTO findById(UUID id) {
        return toDto(repository.findById(id).orElseThrow(this::foodNotFound));
    }

    private FoodItemDTO toDto(Food entity) {
        return FoodItemDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .active(entity.getIsActive() == null || Boolean.TRUE.equals(entity.getIsActive()))
                .build();
    }

    private JMException foodNotFound() {
        Locale locale = LocaleContextHolder.getLocale();
        String messageDetails = messageSource.getMessage(ProblemType.DIET_FOOD_NOT_FOUND.getMessageSource(),
                new Object[] { "" }, locale);
        return new JMException(HttpStatus.BAD_REQUEST.value(), ProblemType.DIET_FOOD_NOT_FOUND.getTitle(),
                ProblemType.DIET_FOOD_NOT_FOUND.getUri(), messageDetails);
    }
}
