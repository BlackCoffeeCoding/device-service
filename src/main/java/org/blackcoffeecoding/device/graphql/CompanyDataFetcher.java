package org.blackcoffeecoding.device.graphql;

import com.netflix.graphql.dgs.*;
import org.blackcoffeecoding.device.api.dto.CompanyRequest;
import org.blackcoffeecoding.device.api.dto.CompanyResponse;
import org.blackcoffeecoding.device.service.CompanyService;

import java.util.List;
import java.util.Map;

@DgsComponent
public class CompanyDataFetcher {

    private final CompanyService companyService;

    public CompanyDataFetcher(CompanyService companyService) {
        this.companyService = companyService;
    }

    @DgsQuery
    public List<CompanyResponse> companies() {
        // Для простоты возвращаем все (без пагинации в этом методе, как в примере)
        return companyService.findAll(0, 100).getContent();
    }

    @DgsQuery
    public CompanyResponse companyById(@InputArgument Long id) {
        return companyService.getById(id);
    }

    @DgsMutation
    public CompanyResponse createCompany(@InputArgument("input") Map<String, String> input) {
        CompanyRequest request = new CompanyRequest(
                input.get("name"),
                input.get("abbreviation")
        );
        return companyService.create(request);
    }

    @DgsMutation
    public Long deleteCompany(@InputArgument Long id) {
        companyService.delete(id);
        return id;
    }
}