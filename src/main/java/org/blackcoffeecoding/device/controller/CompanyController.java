package org.blackcoffeecoding.device.controller;

import org.blackcoffeecoding.device.api.dto.CompanyRequest;
import org.blackcoffeecoding.device.api.dto.CompanyResponse;
import org.blackcoffeecoding.device.api.endpoints.CompanyApi;
import org.blackcoffeecoding.device.assemblers.CompanyModelAssembler;
import org.blackcoffeecoding.device.service.CompanyService;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CompanyController implements CompanyApi {

    private final CompanyService companyService;
    private final CompanyModelAssembler assembler;
    private final PagedResourcesAssembler<CompanyResponse> pagedResourcesAssembler;

    public CompanyController(CompanyService companyService,
                             CompanyModelAssembler assembler,
                             PagedResourcesAssembler<CompanyResponse> pagedResourcesAssembler) {
        this.companyService = companyService;
        this.assembler = assembler;
        this.pagedResourcesAssembler = pagedResourcesAssembler;
    }

    @Override
    public EntityModel<CompanyResponse> getCompany(Long id) {
        return assembler.toModel(companyService.getById(id));
    }

    @Override
    public PagedModel<EntityModel<CompanyResponse>> getAllCompanies(int page, int size) {
        Page<CompanyResponse> companies = companyService.findAll(page, size);
        return pagedResourcesAssembler.toModel(companies, assembler);
    }

    @Override
    public CompanyResponse createCompany(CompanyRequest request) {
        return companyService.create(request);
    }
}