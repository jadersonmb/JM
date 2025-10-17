package com.jm.listeners;

import com.jm.entity.Reminder;
import com.jm.entity.Users;
import com.jm.events.ReminderDueEvent;
import com.jm.repository.ReminderRepository;
import com.jm.services.WhatsAppService;
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
        reminderRepository.findById(event.getReminderId()).ifPresent(this::dispatchReminder);
    }

    private void dispatchReminder(Reminder reminder) {
        Users target = reminder.getTargetUser();
        if (target == null || !StringUtils.hasText(target.getPhoneNumber())) {
            logger.warn("Reminder {} skipped because target phone number is missing", reminder.getId());
            reminder.setActive(Boolean.FALSE);
            reminder.setCompleted(Boolean.FALSE);
            reminder.setDispatchedAt(null);
            reminder.setCompletedAt(null);
            reminderRepository.save(reminder);
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
            reminder.setCompleted(Boolean.TRUE);
            reminder.setCompletedAt(LocalDateTime.now());
            reminder.setActive(Boolean.FALSE);
            reminderRepository.save(reminder);
            logger.info("Reminder {} delivered to {}", reminder.getId(), target.getPhoneNumber());
        } catch (Exception ex) {
            logger.error("Failed to deliver reminder {}: {}", reminder.getId(), ex.getMessage());
            reminder.setDispatchedAt(null);
            reminderRepository.save(reminder);
        }
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
