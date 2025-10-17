package com.jm.mappers;

import com.jm.dto.ReminderDTO;
import com.jm.entity.Reminder;
import com.jm.entity.Users;
import org.mapstruct.BeanMapping;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.time.DayOfWeek;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        builder = @Builder(disableBuilder = true))
public interface ReminderMapper {

    @Mapping(target = "targetUserId", source = "targetUser.id")
    @Mapping(target = "targetUserName", source = "targetUser", qualifiedByName = "mapUserName")
    @Mapping(target = "targetUserPhone", source = "targetUser.phoneNumber")
    @Mapping(target = "createdByUserId", source = "createdBy.id")
    @Mapping(target = "createdByUserName", source = "createdBy", qualifiedByName = "mapUserName")
    @Mapping(target = "repeatWeekdays", source = "repeatWeekdays", qualifiedByName = "splitWeekdays")
    ReminderDTO toDTO(Reminder entity);

    @Mapping(target = "targetUser", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "repeatWeekdays", source = "repeatWeekdays", qualifiedByName = "joinWeekdays")
    Reminder toEntity(ReminderDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "targetUser", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "repeatWeekdays", source = "repeatWeekdays", qualifiedByName = "joinWeekdays")
    void updateEntityFromDto(ReminderDTO dto, @MappingTarget Reminder entity);

    @Named("mapUserName")
    default String mapUserName(Users user) {
        if (user == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        if (user.getName() != null && !user.getName().isBlank()) {
            builder.append(user.getName());
        }
        if (user.getLastName() != null && !user.getLastName().isBlank()) {
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(user.getLastName());
        }
        if (builder.isEmpty() && user.getEmail() != null) {
            builder.append(user.getEmail());
        }
        return builder.isEmpty() ? null : builder.toString();
    }

    @Named("splitWeekdays")
    default List<String> splitWeekdays(String weekdays) {
        if (weekdays == null || weekdays.isBlank()) {
            return List.of();
        }
        return Arrays.stream(weekdays.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> s.toUpperCase(Locale.ROOT))
                .map(this::tryParseDay)
                .filter(Objects::nonNull)
                .distinct()
                .sorted(Comparator.comparingInt(DayOfWeek::getValue))
                .map(day -> day.name().toUpperCase(Locale.ROOT))
                .collect(Collectors.toList());
    }

    @Named("joinWeekdays")
    default String joinWeekdays(List<String> weekdays) {
        if (weekdays == null || weekdays.isEmpty()) {
            return null;
        }
        return weekdays.stream()
                .filter(Objects::nonNull)
                .map(value -> value.trim().toUpperCase(Locale.ROOT))
                .filter(value -> !value.isEmpty())
                .map(this::tryParseDay)
                .filter(Objects::nonNull)
                .distinct()
                .sorted(Comparator.comparingInt(DayOfWeek::getValue))
                .map(day -> day.name().toUpperCase(Locale.ROOT))
                .collect(Collectors.joining(","));
    }

    default DayOfWeek tryParseDay(String value) {
        try {
            return DayOfWeek.valueOf(value);
        } catch (Exception ignored) {
            return null;
        }
    }
}
