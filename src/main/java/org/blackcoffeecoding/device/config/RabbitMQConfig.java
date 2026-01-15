package org.blackcoffeecoding.device.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String EXCHANGE_NAME = "devices-exchange";
    public static final String ROUTING_KEY_CREATED = "device.created";
    public static final String ROUTING_KEY_DELETED = "device.deleted"; // Для самост. работы

    @Bean
    public TopicExchange devicesExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }
}