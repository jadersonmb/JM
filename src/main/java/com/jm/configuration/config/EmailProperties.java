package com.jm.configuration.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "email")
public class EmailProperties {

    /** Sender address used in transactional emails. */
    private String from = "no-reply@jm.com";

    /** Base URL used to compose confirmation links. */
    private String confirmationBaseUrl = "https://app.jm.com/confirm-account";

    /** Base URL used in login buttons for welcome messages. */
    private String loginBaseUrl = "https://app.nutrivision.ai";

    /** Base URL used to compose password recovery links. */
    private String passwordRecoveryBaseUrl = "https://app.nutrivision.ai/reset-password";

    /** Default locale tag applied when the user has no preference configured. */
    private String defaultLanguage = "pt-BR";
}
