package org.blackcoffeecoding.device.controller;

import net.devh.boot.grpc.client.inject.GrpcClient;
import org.blackcoffeecoding.device.api.dto.DeviceResponse;
import org.blackcoffeecoding.device.config.RabbitMQConfig;
import org.blackcoffeecoding.device.events.DeviceRatedEvent;
import org.blackcoffeecoding.device.service.DeviceService;
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
    private final DeviceService deviceService;

    public RatingController(RabbitTemplate rabbitTemplate, DeviceService deviceService) {
        this.rabbitTemplate = rabbitTemplate;
        this.deviceService = deviceService;
    }

    @PostMapping("/{id}/rate")
    public String rateDevice(@PathVariable Long id) {
        try {
            DeviceResponse device = deviceService.getDeviceById(id);
            DeviceRatingRequest request = DeviceRatingRequest.newBuilder()
                    .setDeviceId(id)
                    .setCategory(device.getCategory()) // Можно брать из БД
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