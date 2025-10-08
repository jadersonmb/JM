package com.jm.mappers;

import com.jm.dto.UserConfigurationDTO;
import com.jm.entity.UserConfiguration;
import org.springframework.stereotype.Component;

@Component
public class UserConfigurationMapper {

    public UserConfigurationDTO toDTO(UserConfiguration entity) {
        if (entity == null) {
            return null;
        }
        return UserConfigurationDTO.builder()
                .id(entity.getId())
                .userId(entity.getUser() != null ? entity.getUser().getId() : null)
                .darkMode(entity.isDarkMode())
                .emailNotifications(entity.isEmailNotifications())
                .whatsappNotifications(entity.isWhatsappNotifications())
                .pushNotifications(entity.isPushNotifications())
                .securityAlerts(entity.isSecurityAlerts())
                .productUpdates(entity.isProductUpdates())
                .language(entity.getLanguage())
                .timezone(entity.getTimezone())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public void updateEntity(UserConfiguration entity, UserConfigurationDTO dto) {
        if (entity == null || dto == null) {
            return;
        }
        entity.setDarkMode(dto.isDarkMode());
        entity.setEmailNotifications(dto.isEmailNotifications());
        entity.setWhatsappNotifications(dto.isWhatsappNotifications());
        entity.setPushNotifications(dto.isPushNotifications());
        entity.setSecurityAlerts(dto.isSecurityAlerts());
        entity.setProductUpdates(dto.isProductUpdates());
        entity.setLanguage(dto.getLanguage());
        entity.setTimezone(dto.getTimezone());
    }
}
