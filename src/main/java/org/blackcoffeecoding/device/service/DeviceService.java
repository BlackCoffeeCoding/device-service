package org.blackcoffeecoding.device.service;

import org.blackcoffeecoding.device.api.dto.CompanyResponse;
import org.blackcoffeecoding.device.api.dto.DeviceRequest;
import org.blackcoffeecoding.device.api.dto.DeviceResponse;
import org.blackcoffeecoding.device.api.exception.ResourceAlreadyExistsException; // <-- Новый импорт
import org.blackcoffeecoding.device.api.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class DeviceService {

    private final List<DeviceResponse> devices = new ArrayList<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public DeviceService() {
        // Данные для тестов
        for (int i = 1; i <= 5; i++) {
            devices.add(new DeviceResponse(idGenerator.getAndIncrement(), "Device " + i, "SN-" + i, LocalDate.now(), new CompanyResponse(1L, "Samsung", "SMSNG")));
        }
    }

    public DeviceResponse getDeviceById(Long id) {
        return devices.stream()
                .filter(d -> d.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Device", id));
    }

    public Page<DeviceResponse> findAll(int page, int size) {
        int start = Math.min((int)PageRequest.of(page, size).getOffset(), devices.size());
        int end = Math.min((start + size), devices.size());
        return new PageImpl<>(devices.subList(start, end), PageRequest.of(page, size), devices.size());
    }

    public DeviceResponse createDevice(DeviceRequest request) {
        // Проверка на дубликат Серийного номера
        if (devices.stream().anyMatch(d -> d.getSerialNumber().equals(request.serialNumber()))) {
            throw new ResourceAlreadyExistsException("Устройство", "серийным номером", request.serialNumber());
        }

        DeviceResponse newDevice = new DeviceResponse(
                idGenerator.getAndIncrement(),
                request.name(),
                request.serialNumber(),
                request.releaseDate() != null ? request.releaseDate() : LocalDate.now(),
                new CompanyResponse(request.companyId(), "Test Company", "TST")
        );
        devices.add(newDevice);
        return newDevice;
    }
}