package com.jm.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

@Getter
public class ReminderDueEvent extends ApplicationEvent {

    private final UUID reminderId;
    private final boolean test;

    public ReminderDueEvent(Object source, UUID reminderId, boolean test) {
        super(source);
        this.reminderId = reminderId;
        this.test = test;
    }
}
