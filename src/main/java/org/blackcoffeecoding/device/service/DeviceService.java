package org.blackcoffeecoding.device.service;

import org.blackcoffeecoding.device.api.dto.CompanyResponse;
import org.blackcoffeecoding.device.api.dto.DeviceRequest;
import org.blackcoffeecoding.device.api.dto.DeviceResponse;
import org.blackcoffeecoding.device.api.exception.ResourceAlreadyExistsException;
import org.blackcoffeecoding.device.api.exception.ResourceNotFoundException;
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

    public DeviceService() {
        // Добавляем тестовые данные с категориями
        devices.add(new DeviceResponse(idGenerator.getAndIncrement(), "Samsung S24", "SN-1", "Smartphone", LocalDate.now(), new CompanyResponse(1L, "Samsung", "SMSNG")));
        devices.add(new DeviceResponse(idGenerator.getAndIncrement(), "Samsung Tab", "SN-2", "Tablet", LocalDate.now(), new CompanyResponse(1L, "Samsung", "SMSNG")));
        devices.add(new DeviceResponse(idGenerator.getAndIncrement(), "iPhone 15", "SN-3", "Smartphone", LocalDate.now(), new CompanyResponse(2L, "Apple", "AAPL")));
    }

    public DeviceResponse getDeviceById(Long id) {
        return devices.stream()
                .filter(d -> d.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Device", id));
    }

    // Обновили метод: добавили categoryFilter
    public Page<DeviceResponse> findAll(String categoryFilter, int page, int size) {
        List<DeviceResponse> filteredDevices = devices;

        // Если фильтр передан - фильтруем
        if (categoryFilter != null && !categoryFilter.isBlank()) {
            filteredDevices = devices.stream()
                    .filter(d -> d.getCategory().equalsIgnoreCase(categoryFilter))
                    .collect(Collectors.toList());
        }

        int start = Math.min((int)PageRequest.of(page, size).getOffset(), filteredDevices.size());
        int end = Math.min((start + size), filteredDevices.size());

        // Защита от выхода за границы списка
        if (start > filteredDevices.size()) {
            return new PageImpl<>(new ArrayList<>(), PageRequest.of(page, size), filteredDevices.size());
        }

        return new PageImpl<>(filteredDevices.subList(start, end), PageRequest.of(page, size), filteredDevices.size());
    }

    // Перегрузка для старого REST контроллера (чтобы он не сломался), вызывает новый метод с null
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
                request.category(), // Берем из запроса
                request.releaseDate() != null ? request.releaseDate() : LocalDate.now(),
                new CompanyResponse(request.companyId(), "Test Company", "TST")
        );
        devices.add(newDevice);
        return newDevice;
    }

    // Метод для обновления только категории
    public DeviceResponse updateDeviceCategory(Long id, String newCategory) {
        DeviceResponse existing = getDeviceById(id);

        // Создаем копию с новой категорией (так как DTO обычно неизменяемы или мы имитируем это)
        DeviceResponse updated = new DeviceResponse(
                existing.getId(),
                existing.getName(),
                existing.getSerialNumber(),
                newCategory, // <-- Новая категория
                existing.getReleaseDate(),
                existing.getCompany()
        );

        // Заменяем в списке
        int index = devices.indexOf(existing);
        devices.set(index, updated);

        return updated;
    }

    public void deleteDevice(Long id) {
        // Удаляем устройство, если ID совпадает
        // removeIf возвращает true, если элемент был найден и удален
        boolean removed = devices.removeIf(d -> d.getId().equals(id));

        // (Опционально) Если хочешь кидать ошибку, если удалять нечего:
        if (!removed) {
            throw new ResourceNotFoundException("Устройство", id);
        }
    }
}