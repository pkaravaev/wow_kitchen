package com.foodtech.back.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.stereotype.Component;

/*
* Конфигурация применяемых properties файлов в зависимости текущего профиля */
@Configuration
public class PropertiesProfileConfig {

    @Component
    @Profile("prod")
    @PropertySource(value = {
            "classpath:sms.properties",
            "classpath:crypt.properties",
            "classpath:iiko.properties",
            "classpath:delivery.properties",
            "classpath:push.properties",
            "classpath:payment.properties",
            "classpath:client_messages.properties",
            "classpath:rabbitmq.properties",
            "classpath:payment-prod.properties"
            }, encoding = "UTF-8")
    static class ProductionPropertiesConfig {
        @Bean
        public PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
            return new PropertySourcesPlaceholderConfigurer();
        }
    }

    @Component
    @Profile("dev")
    @PropertySource(value = {
            "classpath:sms.properties",
            "classpath:crypt.properties",
            "classpath:iiko.properties",
            "classpath:delivery.properties",
            "classpath:push.properties",
            "classpath:payment.properties",
            "classpath:client_messages.properties",
            "classpath:rabbitmq.properties",
            "classpath:payment-dev.properties"
    }, encoding = "UTF-8")
    static class DevelopPropertiesConfig {
        @Bean
        public PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
            return new PropertySourcesPlaceholderConfigurer();
        }
    }
}
