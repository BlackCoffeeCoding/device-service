package org.blackcoffeecoding.device.service;

import org.blackcoffeecoding.device.api.dto.CompanyResponse;
import org.blackcoffeecoding.device.api.dto.DeviceRequest;
import org.blackcoffeecoding.device.api.dto.DeviceResponse;
import org.blackcoffeecoding.device.api.exception.ResourceAlreadyExistsException;
import org.blackcoffeecoding.device.api.exception.ResourceNotFoundException;
import org.blackcoffeecoding.device.config.RabbitMQConfig;
import org.blackcoffeecoding.device.events.DeviceCreatedEvent;
import org.blackcoffeecoding.device.events.DeviceDeletedEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class DeviceService {

    private final List<DeviceResponse> devices = new ArrayList<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    private final RabbitTemplate rabbitTemplate;

    public DeviceService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;

        devices.add(new DeviceResponse(idGenerator.getAndIncrement(), "Samsung Galaxy S24", "SN-1", "Smartphone", LocalDate.now(), new CompanyResponse(1L, "Samsung", "SMSNG")));
        devices.add(new DeviceResponse(idGenerator.getAndIncrement(), "iPad Pro", "SN-2", "Tablet", LocalDate.now(), new CompanyResponse(2L, "Apple", "AAPL")));
        devices.add(new DeviceResponse(idGenerator.getAndIncrement(), "Google Pixel 8", "SN-3", "Smartphone", LocalDate.now(), new CompanyResponse(3L, "Google", "GOOG")));
    }

    public DeviceResponse getDeviceById(Long id) {
        return devices.stream()
                .filter(d -> d.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Device", id));
    }

    public Page<DeviceResponse> findAll(String categoryFilter, int page, int size) {
        List<DeviceResponse> filteredDevices = devices;


        if (categoryFilter != null && !categoryFilter.isBlank()) {
            filteredDevices = devices.stream()
                    .filter(d -> d.getCategory().equalsIgnoreCase(categoryFilter))
                    .collect(Collectors.toList());
        }

        int start = Math.min((int) PageRequest.of(page, size).getOffset(), filteredDevices.size());
        int end = Math.min((start + size), filteredDevices.size());

        if (start > filteredDevices.size()) {
            return new PageImpl<>(new ArrayList<>(), PageRequest.of(page, size), filteredDevices.size());
        }

        return new PageImpl<>(filteredDevices.subList(start, end), PageRequest.of(page, size), filteredDevices.size());
    }

    public Page<DeviceResponse> findAll(int page, int size) {
        return findAll(null, page, size);
    }

    public DeviceResponse createDevice(DeviceRequest request) {
        if (devices.stream().anyMatch(d -> d.getSerialNumber().equals(request.serialNumber()))) {
            throw new ResourceAlreadyExistsException("Устройство", "серийным номером", request.serialNumber());
        }

        DeviceResponse newDevice = new DeviceResponse(
                idGenerator.getAndIncrement(),
                request.name(),
                request.serialNumber(),
                request.category(),
                request.releaseDate() != null ? request.releaseDate() : LocalDate.now(),
                new CompanyResponse(request.companyId(), "Test Company", "TST")
        );

        devices.add(newDevice);

        DeviceCreatedEvent event = new DeviceCreatedEvent(
                newDevice.getId(),
                newDevice.getName(),
                newDevice.getSerialNumber(),
                newDevice.getCategory()
        );

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.ROUTING_KEY_CREATED,
                event
        );

        return newDevice;
    }

    public void deleteDevice(Long id) {
        boolean removed = devices.removeIf(d -> d.getId().equals(id));

        if (!removed) {
            throw new ResourceNotFoundException("Устройство", id);
        }

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.ROUTING_KEY_DELETED,
                new DeviceDeletedEvent(id)
        );
    }

    public DeviceResponse updateDeviceCategory(Long id, String newCategory) {
        DeviceResponse existing = getDeviceById(id);

        DeviceResponse updated = new DeviceResponse(
                existing.getId(),
                existing.getName(),
                existing.getSerialNumber(),
                newCategory,
                existing.getReleaseDate(),
                existing.getCompany()
        );

        int index = devices.indexOf(existing);
        devices.set(index, updated);

        return updated;
    }
}