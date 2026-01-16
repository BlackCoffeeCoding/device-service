package org.blackcoffeecoding.device.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Tag(name = "root", description = "Точка входа в API")
@RestController
@RequestMapping("/api")
public class RootController {

    @Operation(summary = "Получить список доступных ресурсов")
    @GetMapping
    public RepresentationModel<?> getRoot() {
        RepresentationModel<?> rootModel = new RepresentationModel<>();

        rootModel.add(linkTo(methodOn(DeviceController.class).getAllDevices(0, 10)).withRel("devices"));

        rootModel.add(linkTo(methodOn(CompanyController.class).getAllCompanies(0, 10)).withRel("companies"));

        rootModel.add(Link.of("http://localhost:8080/swagger-ui/index.html").withRel("documentation"));

        return rootModel;
    }
}