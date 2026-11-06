package org.example.integration.supplier;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class SupplierRestClientConfig {

    @Bean
    public RestClient supplierRestClient() {
        return RestClient.builder()
                .baseUrl("http://localhost:8082")
                .build();
    }

}