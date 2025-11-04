package com.jm.listeners;

import com.jm.entity.Reminder;
import com.jm.entity.Users;
import com.jm.enums.ReminderRepeatMode;
import com.jm.enums.ReminderType;
import com.jm.events.ReminderDueEvent;
import com.jm.repository.ReminderRepository;
import com.jm.services.AnalyticsService;
import com.jm.services.WhatsAppCaptionTemplate;
import com.jm.services.WhatsAppService;
import com.jm.services.support.ReminderScheduleSupport;

import jakarta.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.event.EventListener;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Component
public class ReminderEventListener {

    private static final Logger logger = LoggerFactory.getLogger(ReminderEventListener.class);

    private final ReminderRepository reminderRepository;
    private final WhatsAppService whatsAppService;
    private final AnalyticsService analyticsService;
    private final MessageSource messageSource;

    public ReminderEventListener(ReminderRepository reminderRepository, WhatsAppService whatsAppService,
            AnalyticsService analyticsService, MessageSource messageSource) {
        this.reminderRepository = reminderRepository;
        this.whatsAppService = whatsAppService;
        this.analyticsService = analyticsService;
        this.messageSource = messageSource;
    }

    @Async
    @EventListener
    @Transactional
    public void handleReminderDue(ReminderDueEvent event) {
        reminderRepository.findById(event.getReminderId())
                .ifPresent(reminder -> dispatchReminder(reminder, event.isTest()));
    }

    private void dispatchReminder(Reminder reminder, boolean test) {
        Users target = reminder.getTargetUser();
        if (target == null || !StringUtils.hasText(target.getPhoneNumber())) {
            logger.warn("Reminder {} skipped because target phone number is missing", reminder.getId());
            if (!test) {
                reminder.setActive(Boolean.FALSE);
                reminder.setCompleted(Boolean.FALSE);
                reminder.setDispatchedAt(null);
                reminder.setCompletedAt(null);
                reminderRepository.save(reminder);
            }
            return;
        }

        Locale locale = LocaleContextHolder.getLocale();
        String formattedDate = formatDate(reminder.getScheduledAt(), locale);

        try {
            if (isWaterReminder(reminder)) {
                Map<String, Object> variables = new HashMap<>();
                variables.put("user_name", resolveDisplayName(target));
                BigDecimal fallbackGoal = resolveWaterGoal(reminder);
                AnalyticsService.DailyHydrationSummary hydrationSummary = analyticsService
                        .getTodayHydrationSummary(target.getId())
                        .orElse(null);
                BigDecimal goalMl = hydrationSummary != null ? hydrationSummary.dailyGoalMl() : BigDecimal.ZERO;
                BigDecimal consumedMl = hydrationSummary != null ? hydrationSummary.consumedMl() : BigDecimal.ZERO;

                if (goalMl.compareTo(BigDecimal.ZERO) <= 0 && fallbackGoal.compareTo(BigDecimal.ZERO) > 0) {
                    goalMl = fallbackGoal;
                }

                BigDecimal remainingMl = goalMl.subtract(consumedMl);
                if (remainingMl.compareTo(BigDecimal.ZERO) < 0) {
                    remainingMl = BigDecimal.ZERO;
                }

                variables.put("water_goal", formatMilliliters(goalMl));
                variables.put("water_current", formatMilliliters(consumedMl));
                variables.put("water_remaining", formatMilliliters(remainingMl));
                whatsAppService
                        .sendCaptionMessage(target.getPhoneNumber(), WhatsAppCaptionTemplate.GOLS_WATER_EN, variables)
                        .blockOptional(Duration.ofSeconds(30));
            } else if (reminder.getType() == ReminderType.MEAL) {
                Map<String, Object> variables = new HashMap<>();
                variables.put("user_name", resolveDisplayName(target));
                variables.put("meal_name", reminder.getTitle());
                variables.put("dish_name", StringUtils.hasText(reminder.getDescription()) ? reminder.getDescription().trim()
                        : reminder.getTitle());
                variables.put("kcal", "");
                variables.put("protein", "");
                whatsAppService
                        .sendCaptionMessage(target.getPhoneNumber(), WhatsAppCaptionTemplate.MEAL_REMINDERS_EN, variables)
                        .blockOptional(Duration.ofSeconds(30));
            } else {
                String description = StringUtils.hasText(reminder.getDescription()) ? reminder.getDescription().trim() : null;
                String messageKey = description != null ? "reminder.whatsapp.message.withDescription"
                        : "reminder.whatsapp.message.basic";
                Object[] args = description != null ? new Object[] { reminder.getTitle(), description, formattedDate }
                        : new Object[] { reminder.getTitle(), formattedDate };
                String message = messageSource.getMessage(messageKey, args, locale);
                whatsAppService.sendTextMessage(target.getPhoneNumber(), message)
                        .blockOptional(Duration.ofSeconds(30));
            }
            if (test) {
                logger.info("Reminder {} test delivered to {}", reminder.getId(), target.getPhoneNumber());
                return;
            }
            LocalDateTime dispatchMoment = LocalDateTime.now();
            handleSuccessfulDispatch(reminder, dispatchMoment);
            logger.info("Reminder {} delivered to {}", reminder.getId(), target.getPhoneNumber());
        } catch (Exception ex) {
            logger.error("Failed to deliver reminder {}: {}", reminder.getId(), ex.getMessage());
            if (!test) {
                reminder.setDispatchedAt(null);
                reminderRepository.save(reminder);
            }
        }
    }

    private void handleSuccessfulDispatch(Reminder reminder, LocalDateTime dispatchMoment) {
        ReminderRepeatMode mode = reminder.getRepeatMode() != null ? reminder.getRepeatMode()
                : ReminderRepeatMode.DATE_TIME;
        switch (mode) {
            case DATE_TIME -> {
                reminder.setCompleted(Boolean.TRUE);
                reminder.setCompletedAt(dispatchMoment);
                reminder.setActive(Boolean.FALSE);
            }
            case DAILY_TIME, WEEKLY, INTERVAL -> {
                reminder.setCompleted(Boolean.FALSE);
                reminder.setCompletedAt(null);
                reminder.setActive(Boolean.TRUE);
                LocalDateTime next = ReminderScheduleSupport.determineNextScheduledAt(mode, reminder.getScheduledAt(),
                        reminder.getRepeatTimeOfDay(),
                        ReminderScheduleSupport.parseWeekdays(reminder.getRepeatWeekdays()),
                        reminder.getRepeatIntervalMinutes(), dispatchMoment.plusSeconds(1));
                if (next != null) {
                    reminder.setScheduledAt(next);
                }
            }
            case COUNTDOWN -> {
                Integer remaining = reminder.getRepeatCountRemaining();
                if (remaining == null || remaining <= 1) {
                    reminder.setRepeatCountRemaining(0);
                    reminder.setCompleted(Boolean.TRUE);
                    reminder.setCompletedAt(dispatchMoment);
                    reminder.setActive(Boolean.FALSE);
                } else {
                    reminder.setRepeatCountRemaining(remaining - 1);
                    reminder.setCompleted(Boolean.FALSE);
                    reminder.setCompletedAt(null);
                    reminder.setActive(Boolean.TRUE);
                    LocalDateTime next = ReminderScheduleSupport.determineNextScheduledAt(mode,
                            reminder.getScheduledAt(),
                            reminder.getRepeatTimeOfDay(),
                            ReminderScheduleSupport.parseWeekdays(reminder.getRepeatWeekdays()),
                            reminder.getRepeatIntervalMinutes(), dispatchMoment.plusSeconds(1));
                    if (next != null) {
                        reminder.setScheduledAt(next);
                    }
                }
            }
        }
        reminderRepository.save(reminder);
    }

    private String formatDate(LocalDateTime dateTime, Locale locale) {
        if (dateTime == null) {
            return "";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                .withLocale(locale);
        return formatter.format(dateTime);
    }

    private boolean isWaterReminder(Reminder reminder) {
        if (reminder == null) {
            return false;
        }
        return containsKeyword(reminder.getTitle(), "water", "água", "agua")
                || containsKeyword(reminder.getDescription(), "water", "água", "agua");
    }

    private boolean containsKeyword(String value, String... keywords) {
        if (!StringUtils.hasText(value) || keywords == null) {
            return false;
        }
        String normalized = value.toLowerCase(Locale.ROOT);
        for (String keyword : keywords) {
            if (keyword != null && normalized.contains(keyword.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    private String resolveDisplayName(Users user) {
        if (user == null) {
            return "";
        }
        if (StringUtils.hasText(user.getName())) {
            String[] parts = user.getName().trim().split("\\s+");
            return parts.length > 0 ? parts[0] : user.getName();
        }
        if (StringUtils.hasText(user.getLastName())) {
            return user.getLastName();
        }
        return "";
    }

    private BigDecimal resolveWaterGoal(Reminder reminder) {
        if (reminder == null || !StringUtils.hasText(reminder.getDescription())) {
            return BigDecimal.ZERO;
        }
        String digits = reminder.getDescription().replaceAll("[^0-9]", "");
        if (!StringUtils.hasText(digits)) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(digits);
        } catch (NumberFormatException ex) {
            logger.warn("Unable to parse water goal from reminder {} description: {}", reminder.getId(),
                    reminder.getDescription());
            return BigDecimal.ZERO;
        }
    }

    private String formatMilliliters(BigDecimal value) {
        if (value == null) {
            return "0";
        }
        BigDecimal sanitized = value.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : value;
        return sanitized.setScale(0, RoundingMode.HALF_UP).toPlainString();
    }
}
