package org.blackcoffeecoding.device.graphql;

import com.netflix.graphql.dgs.*;
import org.blackcoffeecoding.device.api.dto.DeviceRequest;
import org.blackcoffeecoding.device.api.dto.DeviceResponse;
import org.blackcoffeecoding.device.service.DeviceService;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.Map;

@DgsComponent
public class DeviceDataFetcher {

    private final DeviceService deviceService;

    public DeviceDataFetcher(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @DgsQuery
    public DeviceResponse deviceById(@InputArgument Long id) {
        return deviceService.getDeviceById(id);
    }

    @DgsQuery
    public Page<DeviceResponse> devices(@InputArgument String categoryFilter,
                                        @InputArgument int page,
                                        @InputArgument int size) {
        return deviceService.findAll(categoryFilter, page, size);
    }

    @DgsMutation
    public DeviceResponse createDevice(@InputArgument("input") Map<String, Object> input) {
        DeviceRequest request = new DeviceRequest(
                (String) input.get("name"),
                (String) input.get("serialNumber"),
                (String) input.get("category"),
                LocalDate.now(),
                Long.parseLong(input.get("companyId").toString())
        );
        return deviceService.createDevice(request);
    }

    @DgsMutation
    public DeviceResponse updateDeviceCategory(@InputArgument Long id,
                                               @InputArgument String category) {
        return deviceService.updateDeviceCategory(id, category);
    }

    @DgsMutation
    public Long deleteDevice(@InputArgument Long id) {
        deviceService.deleteDevice(id);
        return id;
    }
}