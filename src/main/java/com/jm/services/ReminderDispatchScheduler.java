package com.jm.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ReminderDispatchScheduler {

    private static final Logger logger = LoggerFactory.getLogger(ReminderDispatchScheduler.class);

    private final ReminderService reminderService;

    public ReminderDispatchScheduler(ReminderService reminderService) {
        this.reminderService = reminderService;
    }

    @Scheduled(fixedDelayString = "${reminders.scheduler.interval-ms:60000}")
    public void checkDueReminders() {
        logger.trace("Running reminder dispatch scheduler");
        reminderService.dispatchDueReminders();
    }
}
