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

    @GrpcClient("analytics-client")
    private AnalyticsServiceGrpc.AnalyticsServiceBlockingStub analyticsStub;

    private final RabbitTemplate rabbitTemplate;

    public RatingController(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @PostMapping("/{id}/rate")
    public String rateDevice(@PathVariable Long id) {
        try {
            DeviceRatingRequest request = DeviceRatingRequest.newBuilder()
                    .setDeviceId(id)
                    .setCategory("Smartphone") // Можно брать из БД
                    .build();

            DeviceRatingResponse response = analyticsStub.calculateDeviceRating(request);

            DeviceRatedEvent event = new DeviceRatedEvent(
                    response.getDeviceId(),
                    response.getRatingScore(),
                    response.getVerdict()
            );

            rabbitTemplate.convertAndSend(RabbitMQConfig.FANOUT_EXCHANGE, "", event);

            return "Рейтинг рассчитан: " + response.getRatingScore() + " (" + response.getVerdict() + ")";

        } catch (Exception e) {
            return "Ошибка расчета рейтинга (сервис недоступен). Рейтинг: -1";
        }
    }
}