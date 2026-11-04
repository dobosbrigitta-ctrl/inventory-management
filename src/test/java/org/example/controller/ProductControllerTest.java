package org.example.controller;

import org.example.entity.FoodBatch;
import org.example.entity.Product;
import org.example.repository.FoodBatchRepository;
import org.example.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.List;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@SpringBootTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private FoodBatchRepository foodBatchRepository;

    @Test
    @WithMockUser
    void getAllProducts_shouldReturnProductList() throws Exception {

        productRepository.deleteAll();

        Product product = new Product();
        product.setName("Test Apple");
        product.setBarcode("123456789");
        product.setSellingPrice(new BigDecimal("2.50"));
        product.setDiscountPercent(0.0);

        productRepository.save(product);

        mockMvc.perform(get("/api/products")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].name").value("Test Apple"))
                .andExpect(jsonPath("$[0].barcode").value("123456789"))
                .andExpect(jsonPath("$[0].sellingPrice").value(2.50));
    }
    @Test
    @WithMockUser
    void createProductWithBatch_shouldSaveProductAndBatch() throws Exception {

        mockMvc.perform(post("/api/products")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .param("name", "Test Banana")
                        .param("barcode", "987654321")
                        .param("quantity", "10")
                        .param("sellingPrice", "3.50")
                        .param("purchasePrice", "2.00")
                        .param("expirationDate", "2027-12-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Banana"))
                .andExpect(jsonPath("$.barcode").value("987654321"))
                .andExpect(jsonPath("$.sellingPrice").value(3.50));

        Product savedProduct = productRepository
                .findFirstByNameAndBarcodeOrderByIdAsc("Test Banana", "987654321")
                .orElseThrow();

        List<FoodBatch> batches = foodBatchRepository.findAll();

        assertThat(savedProduct.getId()).isNotNull();
        assertThat(batches).hasSize(1);
        assertThat(batches.get(0).getQuantity())
                .isEqualByComparingTo("10");
    }

    @Test
    @WithMockUser
    void createProductWithBatch_shouldReturnBadRequestWhenQuantityIsZero() throws Exception {

        mockMvc.perform(post("/api/products")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .param("name", "Invalid Product")
                        .param("barcode", "000000001")
                        .param("quantity", "0")
                        .param("sellingPrice", "3.50")
                        .param("purchasePrice", "2.00")
                        .param("expirationDate", "2027-12-31"))
                .andExpect(status().isBadRequest());

        assertThat(productRepository
                .findFirstByNameAndBarcodeOrderByIdAsc("Invalid Product", "000000001"))
                .isEmpty();
    }


    @Test
    @WithMockUser
    void deleteProduct_shouldDeleteProductAndBatches() throws Exception {

        foodBatchRepository.deleteAll();
        productRepository.deleteAll();

        Product product = new Product();
        product.setName("Product To Delete");
        product.setBarcode("111222333");
        product.setSellingPrice(new BigDecimal("5.00"));
        product.setDiscountPercent(0.0);

        Product savedProduct = productRepository.save(product);

        FoodBatch batch = new FoodBatch();
        batch.setBatchNumber("TEST-BATCH-001");
        batch.setExpiryDate(java.time.LocalDate.of(2027, 12, 31));
        batch.setQuantity(new BigDecimal("10"));
        batch.setPurchasePrice(new BigDecimal("3.00"));
        batch.setProduct(savedProduct);

        foodBatchRepository.save(batch);

        assertThat(foodBatchRepository.findAll()).hasSize(1);

        mockMvc.perform(delete("/api/products/" + savedProduct.getId())
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isNoContent());

        assertThat(productRepository.existsById(savedProduct.getId())).isFalse();
        assertThat(foodBatchRepository.findAll()).isEmpty();
    }


    @Test
    @WithMockUser
    void deleteProduct_shouldReturnNotFoundWhenProductDoesNotExist() throws Exception {

        mockMvc.perform(delete("/api/products/999999")
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isNotFound());
    }


    @Test
    @WithMockUser
    void updateBatchQuantity_shouldUpdateQuantity() throws Exception {

        Product product = new Product();
        product.setName("Patch Test Product");
        product.setBarcode("555666777");
        product.setSellingPrice(new BigDecimal("5.00"));
        product.setDiscountPercent(0.0);

        Product savedProduct = productRepository.save(product);

        FoodBatch batch = new FoodBatch();
        batch.setBatchNumber("PATCH-BATCH-001");
        batch.setExpiryDate(java.time.LocalDate.of(2027, 12, 31));
        batch.setQuantity(new BigDecimal("10"));
        batch.setPurchasePrice(new BigDecimal("3.00"));
        batch.setProduct(savedProduct);

        FoodBatch savedBatch = foodBatchRepository.save(batch);

        mockMvc.perform(patch("/api/products/batches/" + savedBatch.getId())
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .param("newQuantity", "15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(15));

        FoodBatch updatedBatch = foodBatchRepository.findById(savedBatch.getId())
                .orElseThrow();

        assertThat(updatedBatch.getQuantity()).isEqualByComparingTo("15");
    }

    @Test
    @WithMockUser
    void updateBatchQuantity_shouldReturnNotFoundWhenBatchDoesNotExist() throws Exception {

        mockMvc.perform(patch("/api/products/batches/999999")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .param("newQuantity", "15"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void updateBatchQuantity_shouldReturnBadRequestWhenQuantityIsNegative() throws Exception {

        mockMvc.perform(patch("/api/products/batches/999999")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .param("newQuantity", "-5"))
                .andExpect(status().isBadRequest());
    }


    @Test
    @WithMockUser
    void checkOutGoods_shouldDeductQuantityUsingFefoStrategy() throws Exception {

        foodBatchRepository.deleteAll();
        productRepository.deleteAll();

        Product product = new Product();
        product.setName("FEFO Test Product");
        product.setBarcode("777888999");
        product.setSellingPrice(new BigDecimal("5.00"));
        product.setDiscountPercent(0.0);

        Product savedProduct = productRepository.save(product);

        FoodBatch firstBatch = new FoodBatch();
        firstBatch.setBatchNumber("FEFO-001");
        firstBatch.setExpiryDate(LocalDate.of(2027, 1, 15));
        firstBatch.setQuantity(new BigDecimal("10"));
        firstBatch.setPurchasePrice(new BigDecimal("3.00"));
        firstBatch.setProduct(savedProduct);

        FoodBatch secondBatch = new FoodBatch();
        secondBatch.setBatchNumber("FEFO-002");
        secondBatch.setExpiryDate(LocalDate.of(2027, 3, 20));
        secondBatch.setQuantity(new BigDecimal("20"));
        secondBatch.setPurchasePrice(new BigDecimal("3.00"));
        secondBatch.setProduct(savedProduct);

        FoodBatch thirdBatch = new FoodBatch();
        thirdBatch.setBatchNumber("FEFO-003");
        thirdBatch.setExpiryDate(LocalDate.of(2027, 6, 10));
        thirdBatch.setQuantity(new BigDecimal("30"));
        thirdBatch.setPurchasePrice(new BigDecimal("3.00"));
        thirdBatch.setProduct(savedProduct);

        foodBatchRepository.saveAll(List.of(firstBatch, secondBatch, thirdBatch));

        mockMvc.perform(post("/api/products/" + savedProduct.getId() + "/checkout")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .param("requestedQuantity", "15"))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        "Successfully checked out 15 units using FEFO strategy."));

        assertThat(foodBatchRepository.findById(firstBatch.getId())
                .orElseThrow().getQuantity()).isEqualByComparingTo("0");

        assertThat(foodBatchRepository.findById(secondBatch.getId())
                .orElseThrow().getQuantity()).isEqualByComparingTo("15");

        assertThat(foodBatchRepository.findById(thirdBatch.getId())
                .orElseThrow().getQuantity()).isEqualByComparingTo("30");
    }

    @Test
    @WithMockUser
    void checkOutGoods_shouldReturnBadRequestWhenStockIsInsufficient() throws Exception {

        mockMvc.perform(post("/api/products/999999/checkout")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .param("requestedQuantity", "100"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(
                        "Not enough stock available! Requested: 100, Available: 0"));
    }

    @Test
    @WithMockUser
    void checkOutGoods_shouldReturnBadRequestWhenQuantityIsZero() throws Exception {

        mockMvc.perform(post("/api/products/999999/checkout")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .param("requestedQuantity", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(
                        "Requested quantity must be greater than 0"));
    }

    @Test
    @WithMockUser
    void checkOutGoods_shouldReturnBadRequestWhenQuantityIsNegative() throws Exception {

        mockMvc.perform(post("/api/products/999999/checkout")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .param("requestedQuantity", "-5"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(
                        "Requested quantity must be greater than 0"));
    }



}