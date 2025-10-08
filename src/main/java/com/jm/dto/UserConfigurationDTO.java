package com.jm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserConfigurationDTO {

    private UUID id;
    private UUID userId;
    private boolean darkMode;
    private boolean emailNotifications;
    private boolean whatsappNotifications;
    private boolean pushNotifications;
    private boolean securityAlerts;
    private boolean productUpdates;
    private String language;
    private String timezone;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
