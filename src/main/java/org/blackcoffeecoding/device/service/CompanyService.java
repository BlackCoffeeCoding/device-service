package org.blackcoffeecoding.device.service;

import org.blackcoffeecoding.device.api.dto.CompanyRequest;
import org.blackcoffeecoding.device.api.dto.CompanyResponse;
import org.blackcoffeecoding.device.api.exception.ResourceAlreadyExistsException; // <-- Новый импорт
import org.blackcoffeecoding.device.api.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class CompanyService {

    private final List<CompanyResponse> companies = new ArrayList<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public CompanyService() {
        companies.add(new CompanyResponse(idGenerator.getAndIncrement(), "Apple", "AAPL"));
        companies.add(new CompanyResponse(idGenerator.getAndIncrement(), "Samsung", "SMSNG"));
    }

    public CompanyResponse getById(Long id) {
        return companies.stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Company", id));
    }

    public Page<CompanyResponse> findAll(int page, int size) {
        int start = Math.min((int)PageRequest.of(page, size).getOffset(), companies.size());
        int end = Math.min((start + size), companies.size());
        return new PageImpl<>(companies.subList(start, end), PageRequest.of(page, size), companies.size());
    }

    public CompanyResponse create(CompanyRequest request) {
        // Проверка на дубликат Имени
        if (companies.stream().anyMatch(c -> c.getName().equalsIgnoreCase(request.name()))) {
            throw new ResourceAlreadyExistsException("Компания", "названием", request.name());
        }
        // Проверка на дубликат Аббревиатуры
        if (companies.stream().anyMatch(c -> c.getAbbreviation().equalsIgnoreCase(request.abbreviation()))) {
            throw new ResourceAlreadyExistsException("Компания", "аббревиатурой", request.abbreviation());
        }

        CompanyResponse newCompany = new CompanyResponse(
                idGenerator.getAndIncrement(),
                request.name(),
                request.abbreviation()
        );
        companies.add(newCompany);
        return newCompany;
    }

    public void delete(Long id) {
        boolean removed = companies.removeIf(c -> c.getId().equals(id));

        if (!removed) {
            throw new ResourceNotFoundException("Компания", id);
        }
    }
}