package com.jm.mappers;

import com.jm.dto.PhotoEvolutionDTO;
import com.jm.entity.Image;
import com.jm.entity.PhotoEvolution;
import com.jm.entity.Users;
import org.springframework.stereotype.Component;

@Component
public class PhotoEvolutionMapper {

    public PhotoEvolutionDTO toDTO(PhotoEvolution entity) {
        if (entity == null) {
            return null;
        }
        Image image = entity.getImage();
        Users user = entity.getUser();

        return PhotoEvolutionDTO.builder()
                .id(entity.getId())
                .userId(user != null ? user.getId() : null)
                .userDisplayName(user != null ? buildDisplayName(user) : null)
                .imageId(image != null ? image.getId() : null)
                .imageUrl(image != null ? image.getUrl() : null)
                .imageFileName(image != null ? image.getFileName() : null)
                .bodyPart(entity.getBodyPart())
                .capturedAt(entity.getCapturedAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .weight(entity.getWeight())
                .bodyFatPercentage(entity.getBodyFatPercentage())
                .muscleMass(entity.getMuscleMass())
                .visceralFat(entity.getVisceralFat())
                .waistCircumference(entity.getWaistCircumference())
                .hipCircumference(entity.getHipCircumference())
                .chestCircumference(entity.getChestCircumference())
                .leftArmCircumference(entity.getLeftArmCircumference())
                .rightArmCircumference(entity.getRightArmCircumference())
                .leftThighCircumference(entity.getLeftThighCircumference())
                .rightThighCircumference(entity.getRightThighCircumference())
                .caloricIntake(entity.getCaloricIntake())
                .proteinIntake(entity.getProteinIntake())
                .carbohydrateIntake(entity.getCarbohydrateIntake())
                .fatIntake(entity.getFatIntake())
                .notes(entity.getNotes())
                .build();
    }

    public PhotoEvolution toEntity(PhotoEvolutionDTO dto) {
        if (dto == null) {
            return null;
        }
        PhotoEvolution entity = new PhotoEvolution();
        updateEntityFromDto(dto, entity);
        return entity;
    }

    public void updateEntityFromDto(PhotoEvolutionDTO dto, PhotoEvolution entity) {
        if (dto == null || entity == null) {
            return;
        }
        if (dto.getBodyPart() != null) {
            entity.setBodyPart(dto.getBodyPart());
        }
        entity.setWeight(dto.getWeight());
        entity.setBodyFatPercentage(dto.getBodyFatPercentage());
        entity.setMuscleMass(dto.getMuscleMass());
        entity.setVisceralFat(dto.getVisceralFat());
        entity.setWaistCircumference(dto.getWaistCircumference());
        entity.setHipCircumference(dto.getHipCircumference());
        entity.setChestCircumference(dto.getChestCircumference());
        entity.setLeftArmCircumference(dto.getLeftArmCircumference());
        entity.setRightArmCircumference(dto.getRightArmCircumference());
        entity.setLeftThighCircumference(dto.getLeftThighCircumference());
        entity.setRightThighCircumference(dto.getRightThighCircumference());
        entity.setCaloricIntake(dto.getCaloricIntake());
        entity.setProteinIntake(dto.getProteinIntake());
        entity.setCarbohydrateIntake(dto.getCarbohydrateIntake());
        entity.setFatIntake(dto.getFatIntake());
        entity.setNotes(dto.getNotes());
        entity.setCapturedAt(dto.getCapturedAt());
    }

    private String buildDisplayName(Users user) {
        if (user == null) {
            return null;
        }
        String name = user.getName();
        String lastName = user.getLastName();
        if (name == null) {
            return lastName;
        }
        if (lastName == null || lastName.isBlank()) {
            return name;
        }
        return name + " " + lastName;
    }
}
