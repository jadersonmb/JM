package com.jm.configuration.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@RequiredArgsConstructor
public class PaymentGatewayConfig {

    private final PaymentGatewayProperties properties;

    @Bean(name = "asaasWebClient")
    public WebClient asaasWebClient() {
        WebClient.Builder clientBuilder = WebClient.builder()
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(2 * 1024 * 1024))
                        .build());

        if (StringUtils.hasText(properties.getAsaas().getBaseUrl())) {
            clientBuilder.baseUrl(properties.getAsaas().getBaseUrl());
        }

        if (StringUtils.hasText(properties.getAsaas().getApiKey())) {
            clientBuilder.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getAsaas().getApiKey());
        }

        return clientBuilder.build();
    }
}


