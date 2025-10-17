package com.jm.dto;

import com.jm.enums.ReminderPriority;
import com.jm.enums.ReminderType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReminderDTO {

    private UUID id;
    private String title;
    private String description;
    private LocalDateTime scheduledAt;
    private ReminderPriority priority;
    private ReminderType type;
    private Boolean active;
    private Boolean completed;
    private LocalDateTime dispatchedAt;
    private LocalDateTime completedAt;
    private UUID targetUserId;
    private String targetUserName;
    private String targetUserPhone;
    private UUID createdByUserId;
    private String createdByUserName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
