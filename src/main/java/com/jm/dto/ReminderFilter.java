package com.jm.dto;

import com.jm.enums.ReminderPriority;
import com.jm.enums.ReminderType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReminderFilter {

    private String query;
    private ReminderPriority priority;
    private ReminderType type;
    private Boolean active;
    private UUID targetUserId;
}
