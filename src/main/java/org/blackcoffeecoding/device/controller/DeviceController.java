package org.blackcoffeecoding.device.controller;

import org.blackcoffeecoding.device.api.dto.DeviceRequest;
import org.blackcoffeecoding.device.api.dto.DeviceResponse;
import org.blackcoffeecoding.device.api.endpoints.DeviceApi;
import org.blackcoffeecoding.device.assemblers.DeviceModelAssembler;
import org.blackcoffeecoding.device.service.DeviceService;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DeviceController implements DeviceApi {

    private final DeviceService deviceService;
    private final DeviceModelAssembler deviceModelAssembler;
    private final PagedResourcesAssembler<DeviceResponse> pagedResourcesAssembler;

    public DeviceController(DeviceService deviceService,
                            DeviceModelAssembler deviceModelAssembler,
                            PagedResourcesAssembler<DeviceResponse> pagedResourcesAssembler) {
        this.deviceService = deviceService;
        this.deviceModelAssembler = deviceModelAssembler;
        this.pagedResourcesAssembler = pagedResourcesAssembler;
    }

    @Override
    public EntityModel<DeviceResponse> getDevice(Long id) {
        DeviceResponse device = deviceService.getDeviceById(id);
        // Ассемблер сам добавит ссылки
        return deviceModelAssembler.toModel(device);
    }

    @Override
    public PagedModel<EntityModel<DeviceResponse>> getAllDevices(int page, int size) {
        Page<DeviceResponse> devicesPage = deviceService.findAll(page, size);
        return pagedResourcesAssembler.toModel(devicesPage, deviceModelAssembler);
    }

    @Override
    public EntityModel<DeviceResponse> createDevice(DeviceRequest request) {
        // Создаем через сервис
        DeviceResponse createdDevice = deviceService.createDevice(request);
        return deviceModelAssembler.toModel(createdDevice);
    }

    @Override
    public void deleteDevice(Long id) {
        deviceService.deleteDevice(id);
    }
}