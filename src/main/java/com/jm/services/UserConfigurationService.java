package com.jm.services;

import com.jm.dto.UserConfigurationDTO;
import com.jm.entity.UserConfiguration;
import com.jm.entity.Users;
import com.jm.execption.JMException;
import com.jm.mappers.UserConfigurationMapper;
import com.jm.repository.UserConfigurationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UserConfigurationService {

    private static final Logger logger = LoggerFactory.getLogger(UserConfigurationService.class);

    private final UserConfigurationRepository repository;
    private final UserService userService;
    private final UserConfigurationMapper mapper;

    public UserConfigurationService(UserConfigurationRepository repository, UserService userService,
            UserConfigurationMapper mapper) {
        this.repository = repository;
        this.userService = userService;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public UserConfigurationDTO findByUserId(UUID userId) throws JMException {
        logger.debug("Fetching configuration for user {}", userId);
        Users user = userService.findEntityById(userId);
        return repository.findByUserId(user.getId())
                .map(mapper::toDTO)
                .orElseGet(() -> mapper.toDTO(createDefaultConfiguration(user)));
    }

    @Transactional
    public UserConfigurationDTO update(UUID userId, UserConfigurationDTO dto) throws JMException {
        logger.debug("Updating configuration for user {}", userId);
        Users user = userService.findEntityById(userId);
        UserConfiguration configuration = repository.findByUserId(user.getId())
                .orElseGet(() -> createDefaultConfiguration(user));

        mapper.updateEntity(configuration, dto);
        configuration.setUser(user);

        if (!StringUtils.hasText(configuration.getLanguage())) {
            configuration.setLanguage("pt-BR");
        }
        if (!StringUtils.hasText(configuration.getTimezone())) {
            configuration.setTimezone("America/Sao_Paulo");
        }

        UserConfiguration saved = repository.save(configuration);
        return mapper.toDTO(saved);
    }

    private UserConfiguration createDefaultConfiguration(Users user) {
        LocalDateTime now = LocalDateTime.now();
        return UserConfiguration.builder()
                .user(user)
                .darkMode(false)
                .emailNotifications(true)
                .whatsappNotifications(false)
                .pushNotifications(false)
                .securityAlerts(true)
                .productUpdates(true)
                .language("pt-BR")
                .timezone("America/Sao_Paulo")
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
}
