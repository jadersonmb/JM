package com.jm.services;

import com.jm.dto.NutritionGoalCalculationResponseDTO;
import com.jm.dto.NutritionGoalCreateRequestDTO;
import com.jm.entity.MeasurementUnits;
import com.jm.entity.NutritionGoal;
import com.jm.entity.Users;
import com.jm.enums.BiologicalSex;
import com.jm.enums.NutritionGoalType;
import com.jm.execption.JMException;
import com.jm.mappers.NutritionGoalMapper;
import com.jm.repository.MeasurementUnitRepository;
import com.jm.repository.NutritionGoalRepository;
import com.jm.repository.NutritionGoalTemplateRepository;
import com.jm.repository.UserConfigurationRepository;
import com.jm.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NutritionGoalServiceTest {

    @Mock
    private NutritionGoalRepository repository;

    @Mock
    private NutritionGoalTemplateRepository templateRepository;

    @Mock
    private MeasurementUnitRepository measurementUnitRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserConfigurationRepository userConfigurationRepository;

    private NutritionGoalService service;

    @BeforeEach
    void setUp() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages");
        messageSource.setDefaultEncoding("UTF-8");
        NutritionGoalMapper mapper = Mappers.getMapper(NutritionGoalMapper.class);
        service = new NutritionGoalService(repository, mapper, templateRepository, measurementUnitRepository,
                userRepository, userConfigurationRepository, messageSource);
        LocaleContextHolder.setLocale(new Locale("pt", "BR"));
    }

    @AfterEach
    void tearDown() {
        LocaleContextHolder.resetLocaleContext();
    }

    @Test
    void calculateAndSaveUserGoals_convertsImperialUnitsAndPersistsGoals() {
        UUID userId = UUID.randomUUID();
        Users user = Users.builder().id(userId).build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userConfigurationRepository.findByUserId(userId)).thenReturn(Optional.empty());

        MeasurementUnits kcalUnit = buildUnit("KCAL", MeasurementUnits.UnitType.ENERGY, "kcal");
        MeasurementUnits gramUnit = buildUnit("GRAM", MeasurementUnits.UnitType.WEIGHT, "g");
        when(measurementUnitRepository.findByCodeIgnoreCase("KCAL")).thenReturn(Optional.of(kcalUnit));
        when(measurementUnitRepository.findByCodeIgnoreCase("GRAM")).thenReturn(Optional.empty());
        when(measurementUnitRepository.findByCodeIgnoreCase("G")).thenReturn(Optional.of(gramUnit));

        when(repository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        NutritionGoalCreateRequestDTO request = NutritionGoalCreateRequestDTO.builder()
                .userId(userId)
                .age(30)
                .sex(BiologicalSex.MALE)
                .weight(180.0)
                .weightUnit("lbs")
                .height(5.0)
                .heightUnit("ft")
                .heightInches(9.0)
                .build();

        NutritionGoalCalculationResponseDTO response = service.calculateAndSaveUserGoals(request);

        ArgumentCaptor<Iterable<NutritionGoal>> captor = ArgumentCaptor.forClass(Iterable.class);
        verify(repository).saveAll(captor.capture());
        List<NutritionGoal> savedGoals = StreamSupport.stream(captor.getValue().spliterator(), false)
                .collect(Collectors.toList());

        assertEquals(4, savedGoals.size());
        assertEquals(1770.0, response.getBmr());
        assertEquals(2740.0, response.getTdee());
        assertEquals(2740.0, response.getRecommendedCalories());
        assertEquals(155.0, response.getProtein());
        assertEquals(340.0, response.getCarbs());
        assertEquals(85.0, response.getFat());
        assertNotNull(response.getSummary());
        assertEquals(4, response.getGoals().size());

        NutritionGoal calorieGoal = savedGoals.stream()
                .filter(goal -> goal.getType() == NutritionGoalType.CALORIE_TARGET)
                .findFirst().orElseThrow();
        assertEquals(BigDecimal.valueOf(response.getRecommendedCalories()).setScale(2, RoundingMode.HALF_UP),
                calorieGoal.getTargetValue());
        assertEquals(kcalUnit, calorieGoal.getUnit());
        assertEquals(user, calorieGoal.getCreatedBy());
        assertEquals(calorieGoal.getStartDate().plusYears(1), calorieGoal.getEndDate());
        assertTrue(StringUtils.hasText(calorieGoal.getNotes()));

        NutritionGoal proteinGoal = savedGoals.stream()
                .filter(goal -> goal.getType() == NutritionGoalType.PROTEIN)
                .findFirst().orElseThrow();
        assertEquals(BigDecimal.valueOf(response.getProtein()).setScale(2, RoundingMode.HALF_UP),
                proteinGoal.getTargetValue());
        assertEquals(gramUnit, proteinGoal.getUnit());

        NutritionGoal carbsGoal = savedGoals.stream()
                .filter(goal -> goal.getType() == NutritionGoalType.CARBOHYDRATE)
                .findFirst().orElseThrow();
        assertEquals(BigDecimal.valueOf(response.getCarbs()).setScale(2, RoundingMode.HALF_UP),
                carbsGoal.getTargetValue());

        NutritionGoal fatGoal = savedGoals.stream()
                .filter(goal -> goal.getType() == NutritionGoalType.FAT)
                .findFirst().orElseThrow();
        assertEquals(BigDecimal.valueOf(response.getFat()).setScale(2, RoundingMode.HALF_UP),
                fatGoal.getTargetValue());
    }

    @Test
    void calculateAndSaveUserGoals_invalidSexThrowsException() {
        UUID userId = UUID.randomUUID();
        Users user = Users.builder().id(userId).build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        NutritionGoalCreateRequestDTO request = NutritionGoalCreateRequestDTO.builder()
                .userId(userId)
                .age(28)
                .weight(70.0)
                .weightUnit("kg")
                .height(175.0)
                .heightUnit("cm")
                .build();

        JMException exception = assertThrows(JMException.class, () -> service.calculateAndSaveUserGoals(request));
        assertTrue(StringUtils.hasText(exception.getDetails()));
        verify(repository, never()).saveAll(any());
    }

    private MeasurementUnits buildUnit(String code, MeasurementUnits.UnitType type, String symbol) {
        MeasurementUnits unit = new MeasurementUnits();
        unit.setId(UUID.randomUUID());
        unit.setCode(code);
        unit.setUnitType(type);
        unit.setSymbol(symbol);
        unit.setDescription(symbol);
        unit.setIsActive(Boolean.TRUE);
        return unit;
    }

    private void assertTrue(boolean condition) {
        org.junit.jupiter.api.Assertions.assertTrue(condition);
    }
}
