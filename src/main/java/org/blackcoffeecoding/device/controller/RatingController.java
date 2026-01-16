package org.blackcoffeecoding.device.controller;

import net.devh.boot.grpc.client.inject.GrpcClient;
import org.blackcoffeecoding.device.config.RabbitMQConfig;
import org.blackcoffeecoding.device.events.DeviceRatedEvent;
import org.blackcoffeecoding.grpc.AnalyticsServiceGrpc;
import org.blackcoffeecoding.grpc.DeviceRatingRequest;
import org.blackcoffeecoding.grpc.DeviceRatingResponse;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/devices")
public class RatingController {

    // Внедряем клиент. Имя "analytics-client" должно совпадать с пропертями
    @GrpcClient("analytics-client")
    private AnalyticsServiceGrpc.AnalyticsServiceBlockingStub analyticsStub;

    private final RabbitTemplate rabbitTemplate;

    public RatingController(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @PostMapping("/{id}/rate")
    public String rateDevice(@PathVariable Long id) {
        try {
            // 1. Идем по gRPC в Analytics Service
            DeviceRatingRequest request = DeviceRatingRequest.newBuilder()
                    .setDeviceId(id)
                    .setCategory("Smartphone") // Можно брать из БД
                    .build();

            DeviceRatingResponse response = analyticsStub.calculateDeviceRating(request);

            // 2. Публикуем событие в Fanout Exchange
            DeviceRatedEvent event = new DeviceRatedEvent(
                    response.getDeviceId(),
                    response.getRatingScore(),
                    response.getVerdict()
            );

            // Routing key пустой, так как Fanout его игнорирует
            rabbitTemplate.convertAndSend(RabbitMQConfig.FANOUT_EXCHANGE, "", event);

            return "Рейтинг рассчитан: " + response.getRatingScore() + " (" + response.getVerdict() + ")";

        } catch (Exception e) {
            // Fallback (Задание на отказоустойчивость)
            return "Ошибка расчета рейтинга (сервис недоступен). Рейтинг: -1";
        }
    }
}