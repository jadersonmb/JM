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

    /** Default locale tag applied when the user has no preference configured. */
    private String defaultLanguage = "pt-BR";
}
