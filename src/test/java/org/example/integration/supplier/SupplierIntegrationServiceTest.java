package org.example.integration.supplier;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.example.integration.supplier.dto.SupplierProductDto;
import java.math.BigDecimal;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import java.io.IOException;
import org.springframework.web.client.ResourceAccessException;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.junit.jupiter.api.Assertions.assertNull;

class SupplierIntegrationServiceTest {

    private MockRestServiceServer mockServer;
    private SupplierIntegrationService supplierIntegrationService;

    @BeforeEach
    void setUp() {

        RestClient.Builder restClientBuilder = RestClient.builder()
                .baseUrl("http://localhost:8082");

        mockServer = MockRestServiceServer.bindTo(restClientBuilder).build();

        RestClient restClient = restClientBuilder.build();

        supplierIntegrationService = new SupplierIntegrationService(restClient);
    }

    @Test
void getProduct_returnsProduct_whenSupplierRespondsSuccessfully() {

    String supplierProductId = "SUP-001";

    String responseBody = """
            {
                "supplierProductId": "SUP-001",
                "name": "Milk",
                "purchasePrice": 1.25,
                "availableQuantity": 12.500,
                "supplierName": "Test Supplier"
            }
            """;

    mockServer.expect(requestTo(
                    "http://localhost:8082/api/products/SUP-001"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

    SupplierProductDto result =
            supplierIntegrationService.getProduct(supplierProductId);
            
    
            assertEquals("SUP-001", result.getSupplierProductId());
            assertEquals("Milk", result.getName());
            assertEquals(new BigDecimal("1.25"), result.getPurchasePrice());
            assertEquals(new BigDecimal("12.500"), result.getAvailableQuantity());
            assertEquals("Test Supplier", result.getSupplierName());        
    
            mockServer.verify();
}

    @Test
    void getProduct_throwsException_whenProductNotFound() {

        String supplierProductId = "SUP-404";

        mockServer.expect(requestTo(
                        "http://localhost:8082/api/products/SUP-404"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        HttpClientErrorException exception = assertThrows(
                HttpClientErrorException.class,
                () -> supplierIntegrationService.getProduct(supplierProductId)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        mockServer.verify();
    }
    @Test
    void getProduct_throwsException_whenSupplierServerFails() {

        String supplierProductId = "SUP-500";

        mockServer.expect(requestTo(
                        "http://localhost:8082/api/products/SUP-500"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        HttpServerErrorException exception = assertThrows(
                HttpServerErrorException.class,
                () -> supplierIntegrationService.getProduct(supplierProductId)
        );

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());

        mockServer.verify();
    }

    @Test
    void getProduct_throwsException_whenSupplierReturnsInvalidJson() {

        String supplierProductId = "SUP-INVALID-JSON";

        String invalidJson = "{ invalid json";

        mockServer.expect(requestTo(
                        "http://localhost:8082/api/products/SUP-INVALID-JSON"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(invalidJson, MediaType.APPLICATION_JSON));

        assertThrows(
                RestClientException.class,
                () -> supplierIntegrationService.getProduct(supplierProductId)
        );

        mockServer.verify();
    }
    @Test
    void getProduct_returnsNull_whenSupplierReturnsEmptyBody() {

        String supplierProductId = "SUP-EMPTY";

        mockServer.expect(requestTo(
                        "http://localhost:8082/api/products/SUP-EMPTY"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess());

        SupplierProductDto result =
                supplierIntegrationService.getProduct(supplierProductId);

        assertNull(result);

        mockServer.verify();
    }
    @Test
    void getProduct_throwsException_whenSupplierIsUnavailable() {

        String supplierProductId = "SUP-OFFLINE";

        mockServer.expect(requestTo(
                        "http://localhost:8082/api/products/SUP-OFFLINE"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withException(new IOException("Connection refused")));

        assertThrows(
                ResourceAccessException.class,
                () -> supplierIntegrationService.getProduct(supplierProductId)
        );

        mockServer.verify();
    }
}