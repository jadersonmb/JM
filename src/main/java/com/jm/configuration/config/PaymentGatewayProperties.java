package com.jm.configuration.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "payments")
public class PaymentGatewayProperties {

    private final Stripe stripe = new Stripe();
    private final Asaas asaas = new Asaas();

    @Data
    public static class Stripe {
        /** Secret API key used for server-to-server requests. */
        private String secretKey;
        /** Publishable key required by the frontend for tokenization. */
        private String publishableKey;
        /** Optional webhook secret to verify inbound events. */
        private String webhookSecret;
    }

    @Data
    public static class Asaas {
        /** Authentication token issued by Asaas. */
        private String apiKey;
        /** Pix key registered in Asaas for charge generation. */
        private String pixKey;
        /** URL base for Asaas API. Defaults to production. */
        private String baseUrl = "https://www.asaas.com/api/v3";
        /** Optional token used to validate webhook origin. */
        private String webhookToken;
    }
}
