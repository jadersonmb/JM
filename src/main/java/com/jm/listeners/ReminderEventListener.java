package com.jm.listeners;

import com.jm.entity.Reminder;
import com.jm.entity.Users;
import com.jm.enums.ReminderRepeatMode;
import com.jm.events.ReminderDueEvent;
import com.jm.repository.ReminderRepository;
import com.jm.services.WhatsAppService;
import com.jm.services.support.ReminderScheduleSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.event.EventListener;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

@Component
public class ReminderEventListener {

    private static final Logger logger = LoggerFactory.getLogger(ReminderEventListener.class);

    private final ReminderRepository reminderRepository;
    private final WhatsAppService whatsAppService;
    private final MessageSource messageSource;

    public ReminderEventListener(ReminderRepository reminderRepository, WhatsAppService whatsAppService,
            MessageSource messageSource) {
        this.reminderRepository = reminderRepository;
        this.whatsAppService = whatsAppService;
        this.messageSource = messageSource;
    }

    @Async
    @EventListener
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
        String description = StringUtils.hasText(reminder.getDescription()) ? reminder.getDescription().trim() : null;
        String messageKey = description != null ? "reminder.whatsapp.message.withDescription"
                : "reminder.whatsapp.message.basic";
        Object[] args = description != null ? new Object[] { reminder.getTitle(), description, formattedDate }
                : new Object[] { reminder.getTitle(), formattedDate };
        String message = messageSource.getMessage(messageKey, args, locale);

        try {
            whatsAppService.sendTextMessage(target.getPhoneNumber(), message)
                    .blockOptional(Duration.ofSeconds(30));
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
                        reminder.getRepeatTimeOfDay(), ReminderScheduleSupport.parseWeekdays(reminder.getRepeatWeekdays()),
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
                    LocalDateTime next = ReminderScheduleSupport.determineNextScheduledAt(mode, reminder.getScheduledAt(),
                            reminder.getRepeatTimeOfDay(), ReminderScheduleSupport.parseWeekdays(reminder.getRepeatWeekdays()),
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
}
