
package org.example.controller;

import org.example.entity.FoodBatch;
import org.example.entity.Product;
import org.example.repository.FoodBatchRepository;
import org.example.repository.ProductRepository;
import org.example.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.example.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.example.entity.UserEntity;
import org.springframework.security.core.userdetails.User;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

@SpringBootTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private FoodBatchRepository foodBatchRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @WithMockUser
    void showDashboard_shouldReturnDashboardWithBatches() throws Exception {

        foodBatchRepository.deleteAll();
        productRepository.deleteAll();

        Product product = new Product();
        product.setName("Dashboard Test Apple");
        product.setBarcode("123456789");
        product.setSellingPrice(new BigDecimal("2.50"));
        product.setDiscountPercent(0.0);

        Product savedProduct = productRepository.save(product);

        FoodBatch batch = new FoodBatch();
        batch.setBatchNumber("DASHBOARD-001");
        batch.setExpiryDate(LocalDate.of(2027, 12, 31));
        batch.setQuantity(new BigDecimal("10"));
        batch.setPurchasePrice(new BigDecimal("1.50"));
        batch.setProduct(savedProduct);

        foodBatchRepository.save(batch);

        var result = mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"))
                .andReturn();

        assertThat(result.getModelAndView()).isNotNull();
        assertThat(result.getModelAndView().getModel())
                .containsKeys("batches", "products");

        assertThat(result.getModelAndView().getModel().get("batches"))
                .asList()
                .hasSize(1);

        assertThat(result.getModelAndView().getModel().get("products"))
                .asList()
                .hasSize(1);
    }

    @Test
    @WithMockUser
    void updateBatchFromWeb_shouldUpdateBatchAndProduct() throws Exception {

        Product product = new Product();
        product.setName("Old Product Name");
        product.setBarcode("111111111");
        product.setSellingPrice(new BigDecimal("5.00"));
        product.setDiscountPercent(0.0);

        Product savedProduct = productRepository.save(product);

        FoodBatch batch = new FoodBatch();
        batch.setBatchNumber("UPDATE-001");
        batch.setExpiryDate(LocalDate.of(2027, 12, 31));
        batch.setQuantity(new BigDecimal("10"));
        batch.setPurchasePrice(new BigDecimal("3.00"));
        batch.setProduct(savedProduct);

        FoodBatch savedBatch = foodBatchRepository.save(batch);

        mockMvc.perform(post("/dashboard/batches/" + savedBatch.getId() + "/update")
                        .with(csrf())
                        .param("newQuantity", "20")
                        .param("productName", "Updated Product")
                        .param("barcode", "222222222")
                        .param("sellingPrice", "8.50")
                        .param("purchasePrice", "4.00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        FoodBatch updatedBatch = foodBatchRepository.findById(savedBatch.getId())
                .orElseThrow();

        Product updatedProduct = productRepository.findById(savedProduct.getId())
                .orElseThrow();

        assertThat(updatedBatch.getQuantity()).isEqualByComparingTo("20");
        assertThat(updatedBatch.getPurchasePrice()).isEqualByComparingTo("4.00");
        assertThat(updatedProduct.getName()).isEqualTo("Updated Product");
        assertThat(updatedProduct.getBarcode()).isEqualTo("222222222");
        assertThat(updatedProduct.getSellingPrice()).isEqualByComparingTo("8.50");
    }
    @Test
    @WithMockUser
    void deleteProductFromDashboard_shouldDeleteProductAndBatches() throws Exception {

        Product product = new Product();
        product.setName("Product To Delete");
        product.setBarcode("333444555");
        product.setSellingPrice(new BigDecimal("5.00"));
        product.setDiscountPercent(0.0);

        Product savedProduct = productRepository.save(product);

        FoodBatch batch = new FoodBatch();
        batch.setBatchNumber("DELETE-001");
        batch.setExpiryDate(LocalDate.of(2027, 12, 31));
        batch.setQuantity(new BigDecimal("10"));
        batch.setPurchasePrice(new BigDecimal("3.00"));
        batch.setProduct(savedProduct);

        foodBatchRepository.save(batch);

        mockMvc.perform(post("/dashboard/products/" + savedProduct.getId() + "/delete")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        assertThat(productRepository.existsById(savedProduct.getId())).isFalse();
        assertThat(foodBatchRepository.findAll())
                .noneMatch(savedBatch ->
                        savedBatch.getProduct() != null &&
                                savedBatch.getProduct().getId().equals(savedProduct.getId()));
    }
    @Test
    @WithMockUser
    void showChangePasswordPage_shouldReturnChangePasswordView() throws Exception {

        mockMvc.perform(get("/change-password"))
                .andExpect(status().isOk())
                .andExpect(view().name("change-password"));
    }
    @Test
    void changePassword_shouldUpdatePasswordForUserDetailsPrincipal() throws Exception {

        UserEntity user = new UserEntity();
        user.setUsername("password-test-user");
        user.setPassword(passwordEncoder.encode("OldTestPassword123!"));
        user.setRole("ROLE_USER");

        userRepository.save(user);

        mockMvc.perform(post("/change-password")
                        .with(user(User.withUsername("password-test-user")
                                .password("OldTestPassword123!")
                                .roles("USER")
                                .build()))
                        .with(csrf())
                        .param("newPassword", "NewTestPassword123!"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/change-password?success"));

        UserEntity updatedUser = userRepository.findByUsername("password-test-user")
                .orElseThrow();

        assertTrue(passwordEncoder.matches(
                "NewTestPassword123!",
                updatedUser.getPassword()
        ));
    }


    @Test
    @WithMockUser
    void updateBatchFromWeb_shouldNotUpdateWhenQuantityIsNegative() throws Exception {

        Product product = new Product();
        product.setName("Test Product");
        product.setBarcode("111111111");
        product.setSellingPrice(new BigDecimal("5.00"));
        product.setDiscountPercent(0.0);

        Product savedProduct = productRepository.save(product);

        FoodBatch batch = new FoodBatch();
        batch.setBatchNumber("NEGATIVE-001");
        batch.setExpiryDate(LocalDate.of(2027, 12, 31));
        batch.setQuantity(new BigDecimal("10"));
        batch.setPurchasePrice(new BigDecimal("3.00"));
        batch.setProduct(savedProduct);

        FoodBatch savedBatch = foodBatchRepository.save(batch);

        mockMvc.perform(post("/dashboard/batches/" + savedBatch.getId() + "/update")
                        .with(csrf())
                        .param("newQuantity", "-5")
                        .param("productName", "Changed Product")
                        .param("barcode", "999999999")
                        .param("sellingPrice", "8.50")
                        .param("purchasePrice", "4.00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        FoodBatch unchangedBatch = foodBatchRepository.findById(savedBatch.getId())
                .orElseThrow();

        Product unchangedProduct = productRepository.findById(savedProduct.getId())
                .orElseThrow();

        assertThat(unchangedBatch.getQuantity()).isEqualByComparingTo("10");
        assertThat(unchangedBatch.getPurchasePrice()).isEqualByComparingTo("3.00");
        assertThat(unchangedProduct.getName()).isEqualTo("Test Product");
        assertThat(unchangedProduct.getBarcode()).isEqualTo("111111111");
        assertThat(unchangedProduct.getSellingPrice()).isEqualByComparingTo("5.00");
    }


}
