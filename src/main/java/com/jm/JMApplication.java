package com.jm;

import com.jm.configuration.config.EmailProperties;
import com.jm.configuration.config.PaymentGatewayProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({ PaymentGatewayProperties.class, EmailProperties.class })
public class JMApplication {

    public static void main(String[] args) {
        SpringApplication.run(JMApplication.class, args);
    }
}
