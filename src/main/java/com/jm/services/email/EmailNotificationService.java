package com.jm.services.email;

import com.jm.configuration.config.EmailProperties;
import com.jm.dto.UserDTO;
import com.jm.entity.UserConfiguration;
import com.jm.entity.Users;
import com.jm.repository.UserConfigurationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class EmailNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(EmailNotificationService.class);

    private final UserConfigurationRepository configurationRepository;
    private final EmailTemplateResolver templateResolver;
    private final EmailSender emailSender;
    private final EmailProperties emailProperties;

    public EmailNotificationService(UserConfigurationRepository configurationRepository,
            EmailTemplateResolver templateResolver, EmailSender emailSender, EmailProperties emailProperties) {
        this.configurationRepository = configurationRepository;
        this.templateResolver = templateResolver;
        this.emailSender = emailSender;
        this.emailProperties = emailProperties;
    }

    @Transactional(readOnly = true)
    public void sendUserConfirmation(Users user) {
        if (user == null) {
            return;
        }
        if (!StringUtils.hasText(user.getEmail())) {
            logger.warn("Skipping confirmation email because user {} has no email", user.getId());
            return;
        }
        Optional<UserConfiguration> configuration = configurationRepository.findByUserId(user.getId());
        if (!shouldSendEmail(configuration)) {
            logger.info("Email notifications disabled for user {}", user.getId());
            return;
        }
        Locale locale = resolveLocale(configuration);
        String confirmationLink = buildConfirmationLink(user.getId());
        TemplateEmailContent content = templateResolver.resolve(EmailTemplateType.USER_CONFIRMATION, locale,
                Map.of("name", defaultName(user), "confirmationLink", confirmationLink));
        EmailMessage message = new EmailMessage(user.getEmail(), content.subject(), content.body());
        emailSender.send(message);
    }

    @Transactional(readOnly = true)
    public void sendWelcomeEmail(UserDTO dto) {
        if (dto == null || !StringUtils.hasText(dto.getEmail())) {
            logger.warn("Skipping welcome email because user data is incomplete");
            return;
        }
        Optional<UserConfiguration> configuration = resolveConfiguration(dto);
        if (!shouldSendEmail(configuration)) {
            logger.info("Email notifications disabled for user {}", dto.getId());
            return;
        }
        Locale locale = resolveLocale(dto, configuration);
        String loginUrl = resolveLoginUrl();
        TemplateEmailContent content = templateResolver.resolve(EmailTemplateType.WELCOME, locale,
                Map.of("userName", defaultName(dto), "userEmail", dto.getEmail(),
                        "temporaryPassword", dto.getPassword() != null ? dto.getPassword() : "",
                        "loginUrl", loginUrl));
        EmailMessage message = new EmailMessage(dto.getEmail(), content.subject(), content.body());
        emailSender.send(message);
    }

    @Transactional(readOnly = true)
    public void sendPasswordRecoveryEmail(UserDTO dto, String resetUrl) {
        if (dto == null || !StringUtils.hasText(dto.getEmail())) {
            logger.warn("Skipping password recovery email because user data is incomplete");
            return;
        }
        Optional<UserConfiguration> configuration = resolveConfiguration(dto);
        if (!shouldSendEmail(configuration)) {
            logger.info("Email notifications disabled for user {}", dto.getId());
            return;
        }
        Locale locale = resolveLocale(dto, configuration);
        TemplateEmailContent content = templateResolver.resolve(EmailTemplateType.PASSWORD_RECOVERY, locale,
                Map.of("userName", defaultName(dto), "resetUrl", resetUrl != null ? resetUrl : ""));
        EmailMessage message = new EmailMessage(dto.getEmail(), content.subject(), content.body());
        emailSender.send(message);
    }

    private boolean shouldSendEmail(Optional<UserConfiguration> configuration) {
        return configuration.map(UserConfiguration::isEmailNotifications).orElse(true);
    }

    private Optional<UserConfiguration> resolveConfiguration(UserDTO dto) {
        if (dto != null && dto.getId() != null) {
            return configurationRepository.findByUserId(dto.getId());
        }
        return Optional.empty();
    }

    private Locale resolveLocale(Optional<UserConfiguration> configuration) {
        if (configuration.isPresent() && StringUtils.hasText(configuration.get().getLanguage())) {
            return Locale.forLanguageTag(configuration.get().getLanguage());
        }
        if (StringUtils.hasText(emailProperties.getDefaultLanguage())) {
            return Locale.forLanguageTag(emailProperties.getDefaultLanguage());
        }
        return Locale.getDefault();
    }

    private Locale resolveLocale(UserDTO dto, Optional<UserConfiguration> configuration) {
        if (dto != null && StringUtils.hasText(dto.getLocale())) {
            return Locale.forLanguageTag(dto.getLocale());
        }
        return resolveLocale(configuration);
    }

    private String buildConfirmationLink(UUID userId) {
        String base = emailProperties.getConfirmationBaseUrl();
        if (!StringUtils.hasText(base)) {
            base = "https://app.jm.com/confirm-account";
        }
        if (base.contains("?")) {
            return base + "&userId=" + userId;
        }
        return base + "?userId=" + userId;
    }

    private String defaultName(Users user) {
        if (user == null) {
            return "";
        }
        if (StringUtils.hasText(user.getName())) {
            return user.getName();
        }
        return "Usuário";
    }

    private String defaultName(UserDTO dto) {
        if (dto == null) {
            return "";
        }
        if (StringUtils.hasText(dto.getName())) {
            return dto.getName();
        }
        return "User";
    }

    private String resolveLoginUrl() {
        if (StringUtils.hasText(emailProperties.getLoginBaseUrl())) {
            return emailProperties.getLoginBaseUrl();
        }
        return "https://app.nutrivision.ai";
    }
}
