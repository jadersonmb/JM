package com.jm.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

@Getter
public class ReminderDueEvent extends ApplicationEvent {

    private final UUID reminderId;

    public ReminderDueEvent(Object source, UUID reminderId) {
        super(source);
        this.reminderId = reminderId;
    }
}
