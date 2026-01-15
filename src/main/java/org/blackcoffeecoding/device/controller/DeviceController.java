package org.blackcoffeecoding.device.controller;

import org.blackcoffeecoding.device.api.dto.DeviceRequest;
import org.blackcoffeecoding.device.api.dto.DeviceResponse;
import org.blackcoffeecoding.device.api.endpoints.DeviceApi;
import org.blackcoffeecoding.device.service.DeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController // Обрабатывает HTTP запросы [cite: 200]
public class DeviceController implements DeviceApi {

    private final DeviceService deviceService;

    // Внедряем сервис через конструктор [cite: 215]
    @Autowired
    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Override
    public DeviceResponse getDevice(Long id) {
        // Просто делегируем работу сервису
        return deviceService.getDeviceById(id);
    }

    @Override
    public DeviceResponse createDevice(DeviceRequest request) {
        // Просто делегируем работу сервису
        return deviceService.createDevice(request);
    }
}