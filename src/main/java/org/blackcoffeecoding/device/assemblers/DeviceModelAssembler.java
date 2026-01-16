package org.blackcoffeecoding.device.assemblers;

import org.blackcoffeecoding.device.api.dto.DeviceResponse;
import org.blackcoffeecoding.device.controller.DeviceController;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class DeviceModelAssembler implements RepresentationModelAssembler<DeviceResponse, EntityModel<DeviceResponse>> {

    @Override
    public EntityModel<DeviceResponse> toModel(DeviceResponse device) {
        return EntityModel.of(device,
                linkTo(methodOn(DeviceController.class).getDevice(device.getId())).withSelfRel(),
                linkTo(methodOn(DeviceController.class).getAllDevices(0, 10)).withRel("devices")
        );
    }
}