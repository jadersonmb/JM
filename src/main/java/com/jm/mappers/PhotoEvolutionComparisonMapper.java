package com.jm.mappers;

import com.jm.dto.PhotoEvolutionEvolutionPointDTO;
import com.jm.entity.PhotoEvolution;
import com.jm.enums.BodyPart;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;
import java.util.List;

@Mapper(componentModel = "spring")
public interface PhotoEvolutionComparisonMapper {

    @Mapping(target = "date", source = "capturedAt")
    @Mapping(target = "imageUrl", source = "image.url")
    @Mapping(target = "measurement", expression = "java(resolveMeasurement(entity))")
    PhotoEvolutionEvolutionPointDTO toPoint(PhotoEvolution entity);

    List<PhotoEvolutionEvolutionPointDTO> toPoints(List<PhotoEvolution> entities);

    default Double resolveMeasurement(PhotoEvolution entity) {
        if (entity == null) {
            return null;
        }
        BodyPart bodyPart = entity.getBodyPart();
        if (bodyPart == null) {
            return toDouble(entity.getWeight());
        }
        return switch (bodyPart) {
            case ABDOMEN -> toDouble(entity.getWaistCircumference());
            case ARMS -> average(entity.getLeftArmCircumference(), entity.getRightArmCircumference());
            case LEGS -> average(entity.getLeftThighCircumference(), entity.getRightThighCircumference());
            case BACK -> toDouble(entity.getChestCircumference());
            case SHOULDERS -> toDouble(entity.getChestCircumference());
            case FULL_BODY -> toDouble(entity.getWeight());
            case FRONT, LEFT_SIDE, RIGHT_SIDE -> toDouble(entity.getBodyFatPercentage());
        };
    }

    default Double average(BigDecimal first, BigDecimal second) {
        Double left = toDouble(first);
        Double right = toDouble(second);
        if (left == null && right == null) {
            return null;
        }
        if (left == null) {
            return right;
        }
        if (right == null) {
            return left;
        }
        return (left + right) / 2.0;
    }

    default Double toDouble(BigDecimal value) {
        if (value == null) {
            return null;
        }
        return value.doubleValue();
    }
}
