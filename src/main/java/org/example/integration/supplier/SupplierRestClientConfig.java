package org.example.integration.supplier;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class SupplierRestClientConfig {

    @Value("${supplier.api.base-url}")
    private String supplierApiBaseUrl;

    @Bean
    public RestClient supplierRestClient() {
        return RestClient.builder()
                .baseUrl(supplierApiBaseUrl)
                .build();
    }
}