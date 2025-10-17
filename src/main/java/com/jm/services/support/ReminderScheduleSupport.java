package com.jm.services.support;

import com.jm.enums.ReminderRepeatMode;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public final class ReminderScheduleSupport {

    private ReminderScheduleSupport() {
    }

    public static Set<DayOfWeek> normalizeWeekdays(Collection<String> weekdays) {
        if (weekdays == null || weekdays.isEmpty()) {
            return Collections.emptySet();
        }
        return weekdays.stream()
                .filter(Objects::nonNull)
                .map(value -> value.trim().toUpperCase(Locale.ROOT))
                .map(ReminderScheduleSupport::safeParseDay)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public static Set<DayOfWeek> parseWeekdays(String data) {
        if (data == null || data.isBlank()) {
            return Collections.emptySet();
        }
        String[] split = data.split(",");
        Set<DayOfWeek> result = new LinkedHashSet<>();
        for (String value : split) {
            DayOfWeek day = safeParseDay(value);
            if (day != null) {
                result.add(day);
            }
        }
        return result;
    }

    public static LocalDateTime determineNextScheduledAt(ReminderRepeatMode mode,
            LocalDateTime providedScheduledAt,
            LocalTime timeOfDay,
            Set<DayOfWeek> weekdays,
            Integer intervalMinutes,
            LocalDateTime reference) {
        ReminderRepeatMode effectiveMode = mode != null ? mode : ReminderRepeatMode.DATE_TIME;
        LocalDateTime effectiveReference = reference != null ? reference : LocalDateTime.now();

        return switch (effectiveMode) {
            case DATE_TIME -> providedScheduledAt;
            case DAILY_TIME -> calculateNextDailyOccurrence(timeOfDay, providedScheduledAt, effectiveReference);
            case WEEKLY -> calculateNextWeeklyOccurrence(weekdays, timeOfDay, providedScheduledAt, effectiveReference);
            case INTERVAL, COUNTDOWN -> calculateNextIntervalOccurrence(intervalMinutes, providedScheduledAt, effectiveReference);
        };
    }

    public static LocalDateTime calculateNextDailyOccurrence(LocalTime timeOfDay, LocalDateTime provided,
            LocalDateTime reference) {
        if (timeOfDay == null) {
            return provided;
        }
        LocalDateTime ref = reference != null ? reference : LocalDateTime.now();
        if (provided != null && provided.isAfter(ref)) {
            return provided;
        }
        LocalDateTime candidate = LocalDateTime.of(ref.toLocalDate(), timeOfDay);
        while (!candidate.isAfter(ref)) {
            candidate = candidate.plusDays(1);
        }
        return candidate;
    }

    public static LocalDateTime calculateNextWeeklyOccurrence(Set<DayOfWeek> weekdays, LocalTime timeOfDay,
            LocalDateTime provided, LocalDateTime reference) {
        if (weekdays == null || weekdays.isEmpty() || timeOfDay == null) {
            return provided;
        }
        LocalDateTime ref = reference != null ? reference : LocalDateTime.now();
        if (provided != null && provided.isAfter(ref) && weekdays.contains(provided.getDayOfWeek())) {
            return LocalDateTime.of(provided.toLocalDate(), timeOfDay);
        }
        LocalDate baseDate = ref.toLocalDate();
        LocalDateTime candidate = LocalDateTime.of(baseDate, timeOfDay);
        for (int i = 0; i < 14; i++) {
            DayOfWeek day = candidate.toLocalDate().getDayOfWeek();
            if (weekdays.contains(day) && candidate.isAfter(ref)) {
                return candidate;
            }
            candidate = candidate.plusDays(1).with(timeOfDay);
        }
        return candidate;
    }

    public static LocalDateTime calculateNextIntervalOccurrence(Integer intervalMinutes, LocalDateTime provided,
            LocalDateTime reference) {
        if (intervalMinutes == null || intervalMinutes <= 0) {
            return provided;
        }
        LocalDateTime ref = reference != null ? reference : LocalDateTime.now();
        LocalDateTime candidate = provided != null && provided.isAfter(ref) ? provided : ref.plusMinutes(intervalMinutes);
        if (!candidate.isAfter(ref)) {
            candidate = ref.plusMinutes(intervalMinutes);
        }
        return candidate;
    }

    private static DayOfWeek safeParseDay(String value) {
        if (value == null) {
            return null;
        }
        try {
            return DayOfWeek.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
