package org.blackcoffeecoding.device.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String EXCHANGE_NAME = "devices-exchange";
    public static final String ROUTING_KEY_CREATED = "device.created";
    public static final String ROUTING_KEY_DELETED = "device.deleted";
    public static final String FANOUT_EXCHANGE = "analytics-fanout";

    @Bean
    public TopicExchange devicesExchange() {
        // durable=true (true, false) означает, что обменник переживет перезагрузку RabbitMQ [cite: 833-834]
        return new TopicExchange(EXCHANGE_NAME, true, false);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        // Используем Jackson для красивого JSON
        return new Jackson2JsonMessageConverter(new ObjectMapper().findAndRegisterModules());
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, Jackson2JsonMessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);

        // Callback: Сработает, когда брокер подтвердит (или отвергнет) получение сообщения [cite: 844-849]
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (!ack) {
                System.err.println("NACK: Сообщение не доставлено брокеру! Причина: " + cause);
            }
        });

        return rabbitTemplate;
    }

    @Bean
    public FanoutExchange analyticsExchange() {
        // Fanout рассылает всем, игнорируя routing key
        return new FanoutExchange(FANOUT_EXCHANGE, true, false);
    }
}