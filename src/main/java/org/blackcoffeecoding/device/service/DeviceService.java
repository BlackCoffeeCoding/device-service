package org.blackcoffeecoding.device.service;

import org.blackcoffeecoding.device.api.dto.CompanyResponse;
import org.blackcoffeecoding.device.api.dto.DeviceRequest;
import org.blackcoffeecoding.device.api.dto.DeviceResponse;
import org.blackcoffeecoding.device.api.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service // Говорим Спрингу, что это сервисный слой [cite: 206]
public class DeviceService {

    // Имитация базы данных в памяти
    private final List<DeviceResponse> devices = new ArrayList<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public DeviceService() {
        // Добавим одно тестовое устройство сразу, чтобы было что получать
        devices.add(new DeviceResponse(
                idGenerator.getAndIncrement(),
                "Samsung Galaxy S24",
                "SN-SAMSUNG-001",
                LocalDate.of(2024, 1, 17),
                new CompanyResponse(1L, "Samsung", "SMSNG")
        ));
    }

    public DeviceResponse getDeviceById(Long id) {
        // Ищем устройство в списке. Если нет - кидаем ошибку из нашего КОНТРАКТА
        return devices.stream()
                .filter(d -> d.id().equals(id))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Device", id));
    }

    public DeviceResponse createDevice(DeviceRequest request) {
        // Создаем новое устройство на основе запроса
        DeviceResponse newDevice = new DeviceResponse(
                idGenerator.getAndIncrement(),
                request.name(),
                request.serialNumber(),
                request.releaseDate() != null ? request.releaseDate() : LocalDate.now(),
                // В реальности мы бы искали компанию в БД по ID, но пока сделаем заглушку
                new CompanyResponse(request.companyId(), "Test Company", "TST")
        );

        devices.add(newDevice);
        return newDevice;
    }
}