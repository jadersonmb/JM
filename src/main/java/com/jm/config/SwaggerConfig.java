package com.jm.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.i18n.LocaleContextHolder;

@Configuration
@SecurityScheme(
        name = "BearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class SwaggerConfig {

    private final MessageSource messageSource;

    public SwaggerConfig(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Bean
    public OpenAPI nutrivisionOpenAPI() {
        String localizedDescription = message("swagger.description",
                "API documentation for NutriVision AI — AI-powered nutrition assistant.");
        String detailedDescription = """
                This documentation provides all endpoints for:
                - Users and Authentication (OAuth2 / JWT)
                - Nutrition Analysis, Anamnesis, Diet, Goals
                - Photo Evolution, Recurring Payments, and Roles/Permissions
                """;

        return new OpenAPI()
                .info(new Info()
                        .title(message("swagger.title", "NutriVision AI API"))
                        .description(localizedDescription + "\n\n" + detailedDescription.strip())
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name(message("swagger.contact.name", "NutriVision AI Support"))
                                .email(message("swagger.contact.email", "support@nutrivision.ai"))
                                .url("https://nutrivision.ai"))
                        .license(new License()
                                .name(message("swagger.license", "MIT"))
                                .url("https://opensource.org/licenses/MIT")))
                .externalDocs(new ExternalDocumentation()
                        .description("NutriVision AI Documentation")
                        .url("https://docs.nutrivision.ai"))
                .addSecurityItem(new SecurityRequirement().addList("BearerAuth"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("BearerAuth",
                                new io.swagger.v3.oas.models.security.SecurityScheme()
                                        .name("Authorization")
                                        .type(io.swagger.v3.oas.models.security.SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .in(io.swagger.v3.oas.models.security.SecurityScheme.In.HEADER)
                        ));
    }

    private String message(String code, String defaultMessage) {
        return messageSource.getMessage(code, null, defaultMessage, LocaleContextHolder.getLocale());
    }

}
