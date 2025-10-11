package com.jm.mappers;

import com.jm.dto.AnamnesisBiochemicalResultDTO;
import com.jm.dto.AnamnesisDTO;
import com.jm.dto.AnamnesisFoodPreferenceDTO;
import com.jm.dto.AnamnesisFoodRecallDTO;
import com.jm.dto.AnamnesisFoodRecallItemDTO;
import com.jm.entity.Anamnesis;
import com.jm.entity.AnamnesisBiochemicalResult;
import com.jm.entity.AnamnesisFoodPreference;
import com.jm.entity.AnamnesisFoodRecall;
import com.jm.entity.AnamnesisFoodRecallItem;
import com.jm.entity.BiochemicalExam;
import com.jm.entity.Food;
import com.jm.entity.MeasurementUnits;
import com.jm.entity.Pathology;
import com.jm.entity.Users;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class AnamnesisMapper {

    public AnamnesisDTO toDTO(Anamnesis entity) {
        if (entity == null) {
            return null;
        }
        AnamnesisDTO dto = new AnamnesisDTO();
        BeanUtils.copyProperties(entity, dto, "pathologies", "biochemicalResults", "foodPreferences", "foodRecalls");

        dto.setPathologyIds(entity.getPathologies().stream()
                .map(Pathology::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet()));

        dto.setBiochemicalResults(entity.getBiochemicalResults().stream()
                .map(this::mapBiochemicalResultToDto)
                .collect(Collectors.toList()));

        dto.setFoodPreferences(entity.getFoodPreferences().stream()
                .map(this::mapFoodPreferenceToDto)
                .collect(Collectors.toList()));

        dto.setFoodRecalls(entity.getFoodRecalls().stream()
                .map(this::mapFoodRecallToDto)
                .collect(Collectors.toList()));

        if (entity.getUser() != null) {
            Users user = entity.getUser();
            dto.setUserId(user.getId());
            dto.setCountryId(user.getCountry() != null ? user.getCountry().getId() : null);
            dto.setCountryName(user.getCountry() != null ? user.getCountry().getName() : null);
            dto.setCityId(user.getCity() != null ? user.getCity().getId() : null);
            dto.setCityName(user.getCity() != null ? user.getCity().getName() : null);
            dto.setEducationLevelId(user.getEducationLevel() != null ? user.getEducationLevel().getId() : null);
            dto.setProfessionId(user.getProfession() != null ? user.getProfession().getId() : null);
            dto.setPatientName(buildFullName(user));
            dto.setAddress(buildAddress(user));
            dto.setBirthDate(user.getBirthDate());
            dto.setAge(user.getAge());
            dto.setPhoneNumber(user.getPhoneNumber());
            dto.setEducationLevelName(user.getEducationLevel() != null ? user.getEducationLevel().getName() : dto.getEducationLevelName());
            dto.setProfessionName(user.getProfession() != null ? user.getProfession().getName() : dto.getProfessionName());
            dto.setConsultationGoal(user.getConsultationGoal());
        }

        return dto;
    }

    public Anamnesis toEntity(AnamnesisDTO dto) {
        if (dto == null) {
            return null;
        }
        Anamnesis entity = new Anamnesis();
        BeanUtils.copyProperties(dto, entity, "pathologyIds", "biochemicalResults", "foodPreferences", "foodRecalls", "userId");

        Set<Pathology> pathologies = dto.getPathologyIds() == null
                ? new HashSet<>()
                : dto.getPathologyIds().stream()
                        .filter(Objects::nonNull)
                        .map(id -> {
                            Pathology pathology = new Pathology();
                            pathology.setId(id);
                            return pathology;
                        })
                        .collect(Collectors.toSet());
        entity.setPathologies(pathologies);

        List<AnamnesisBiochemicalResult> biochemicalResults = dto.getBiochemicalResults() == null
                ? new ArrayList<>()
                : dto.getBiochemicalResults().stream()
                        .map(this::mapBiochemicalResultToEntity)
                        .collect(Collectors.toList());
        biochemicalResults.forEach(result -> result.setAnamnesis(entity));
        entity.setBiochemicalResults(biochemicalResults);

        List<AnamnesisFoodPreference> foodPreferences = dto.getFoodPreferences() == null
                ? new ArrayList<>()
                : dto.getFoodPreferences().stream()
                        .map(this::mapFoodPreferenceToEntity)
                        .collect(Collectors.toList());
        foodPreferences.forEach(preference -> preference.setAnamnesis(entity));
        entity.setFoodPreferences(foodPreferences);

        List<AnamnesisFoodRecall> recalls = dto.getFoodRecalls() == null
                ? new ArrayList<>()
                : dto.getFoodRecalls().stream()
                        .map(this::mapFoodRecallToEntity)
                        .collect(Collectors.toList());
        recalls.forEach(recall -> {
            recall.setAnamnesis(entity);
            recall.getItems().forEach(item -> item.setFoodRecall(recall));
        });
        entity.setFoodRecalls(recalls);

        return entity;
    }

    private AnamnesisBiochemicalResultDTO mapBiochemicalResultToDto(AnamnesisBiochemicalResult entity) {
        if (entity == null) {
            return null;
        }
        return AnamnesisBiochemicalResultDTO.builder()
                .id(entity.getId())
                .biochemicalExamId(entity.getBiochemicalExam() != null ? entity.getBiochemicalExam().getId() : null)
                .biochemicalExamName(entity.getBiochemicalExam() != null ? entity.getBiochemicalExam().getName() : null)
                .resultValue(entity.getResultValue())
                .resultDate(entity.getResultDate())
                .build();
    }

    private AnamnesisBiochemicalResult mapBiochemicalResultToEntity(AnamnesisBiochemicalResultDTO dto) {
        if (dto == null) {
            return null;
        }
        AnamnesisBiochemicalResult entity = new AnamnesisBiochemicalResult();
        entity.setId(dto.getId() != null ? dto.getId() : UUID.randomUUID());
        entity.setResultValue(dto.getResultValue());
        entity.setResultDate(dto.getResultDate());
        if (dto.getBiochemicalExamId() != null) {
            BiochemicalExam exam = new BiochemicalExam();
            exam.setId(dto.getBiochemicalExamId());
            entity.setBiochemicalExam(exam);
        } else {
            entity.setBiochemicalExam(null);
        }
        return entity;
    }

    private AnamnesisFoodPreferenceDTO mapFoodPreferenceToDto(AnamnesisFoodPreference entity) {
        if (entity == null) {
            return null;
        }
        return AnamnesisFoodPreferenceDTO.builder()
                .id(entity.getId())
                .foodId(entity.getFood() != null ? entity.getFood().getId() : null)
                .foodName(entity.getFood() != null ? entity.getFood().getName() : null)
                .preferenceType(entity.getPreferenceType())
                .notes(entity.getNotes())
                .build();
    }

    private AnamnesisFoodPreference mapFoodPreferenceToEntity(AnamnesisFoodPreferenceDTO dto) {
        if (dto == null) {
            return null;
        }
        AnamnesisFoodPreference entity = new AnamnesisFoodPreference();
        entity.setId(dto.getId() != null ? dto.getId() : UUID.randomUUID());
        entity.setPreferenceType(dto.getPreferenceType());
        entity.setNotes(dto.getNotes());
        if (dto.getFoodId() != null) {
            Food food = new Food();
            food.setId(dto.getFoodId());
            entity.setFood(food);
        } else {
            entity.setFood(null);
        }
        return entity;
    }

    private AnamnesisFoodRecallDTO mapFoodRecallToDto(AnamnesisFoodRecall entity) {
        if (entity == null) {
            return null;
        }
        return AnamnesisFoodRecallDTO.builder()
                .id(entity.getId())
                .mealName(entity.getMealName())
                .observation(entity.getObservation())
                .items(entity.getItems().stream()
                        .map(this::mapFoodRecallItemToDto)
                        .collect(Collectors.toList()))
                .build();
    }

    private AnamnesisFoodRecall mapFoodRecallToEntity(AnamnesisFoodRecallDTO dto) {
        if (dto == null) {
            return null;
        }
        AnamnesisFoodRecall entity = new AnamnesisFoodRecall();
        entity.setId(dto.getId() != null ? dto.getId() : UUID.randomUUID());
        entity.setMealName(dto.getMealName());
        entity.setObservation(dto.getObservation());
        List<AnamnesisFoodRecallItem> items = dto.getItems() == null
                ? new ArrayList<>()
                : dto.getItems().stream()
                        .map(this::mapFoodRecallItemToEntity)
                        .collect(Collectors.toList());
        entity.setItems(items);
        return entity;
    }

    private AnamnesisFoodRecallItemDTO mapFoodRecallItemToDto(AnamnesisFoodRecallItem entity) {
        if (entity == null) {
            return null;
        }
        return AnamnesisFoodRecallItemDTO.builder()
                .id(entity.getId())
                .foodId(entity.getFood() != null ? entity.getFood().getId() : null)
                .foodName(entity.getFood() != null ? entity.getFood().getName() : null)
                .measurementUnitId(entity.getMeasurementUnit() != null ? entity.getMeasurementUnit().getId() : null)
                .measurementUnitSymbol(entity.getMeasurementUnit() != null ? entity.getMeasurementUnit().getSymbol() : null)
                .quantity(entity.getQuantity())
                .build();
    }

    private AnamnesisFoodRecallItem mapFoodRecallItemToEntity(AnamnesisFoodRecallItemDTO dto) {
        if (dto == null) {
            return null;
        }
        AnamnesisFoodRecallItem entity = new AnamnesisFoodRecallItem();
        entity.setId(dto.getId() != null ? dto.getId() : UUID.randomUUID());
        entity.setQuantity(dto.getQuantity());

        if (dto.getFoodId() != null) {
            Food food = new Food();
            food.setId(dto.getFoodId());
            entity.setFood(food);
        } else {
            entity.setFood(null);
        }

        if (dto.getMeasurementUnitId() != null) {
            MeasurementUnits unit = new MeasurementUnits();
            unit.setId(dto.getMeasurementUnitId());
            entity.setMeasurementUnit(unit);
        } else {
            entity.setMeasurementUnit(null);
        }
        return entity;
    }

    private String buildAddress(Users user) {
        List<String> parts = new ArrayList<>();
        if (StringUtils.hasText(user.getStreet())) {
            parts.add(user.getStreet());
        }
        if (user.getCity() != null && StringUtils.hasText(user.getCity().getName())) {
            parts.add(user.getCity().getName());
        }
        if (StringUtils.hasText(user.getState())) {
            parts.add(user.getState());
        }
        if (user.getCountry() != null && StringUtils.hasText(user.getCountry().getName())) {
            parts.add(user.getCountry().getName());
        }
        return String.join(", ", parts);
    }

    private String buildFullName(Users user) {
        String firstName = user.getName();
        String lastName = user.getLastName();
        if (!StringUtils.hasText(firstName)) {
            return StringUtils.hasText(lastName) ? lastName : "";
        }
        if (!StringUtils.hasText(lastName)) {
            return firstName;
        }
        return String.format("%s %s", firstName, lastName).trim();
    }
}
