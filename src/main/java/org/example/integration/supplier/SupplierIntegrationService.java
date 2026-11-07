package org.example.integration.supplier;

import org.springframework.web.client.RestClient;
import org.springframework.stereotype.Service;
import org.example.integration.supplier.dto.SupplierProductDto;

@Service
public class SupplierIntegrationService {

    private final RestClient restClient;

    public SupplierIntegrationService(RestClient restClient) {
        this.restClient = restClient;
    }

    public SupplierProductDto getProduct(String supplierProductId) {
        return restClient.get()
                .uri("/api/products/{id}", supplierProductId)
                .retrieve()
                .body(SupplierProductDto.class);
    }
}