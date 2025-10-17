package com.jm;

import com.jm.configuration.config.EmailProperties;
import com.jm.configuration.config.PaymentGatewayProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties({ PaymentGatewayProperties.class, EmailProperties.class })
@EnableScheduling
public class JMApplication {

    public static void main(String[] args) {
        SpringApplication.run(JMApplication.class, args);
    }
}
