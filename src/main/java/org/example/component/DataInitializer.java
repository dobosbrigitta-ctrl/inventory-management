package org.example.component;

import org.example.entity.Product;
import org.example.entity.FoodBatch;
import org.example.repository.FoodBatchRepository;
import org.example.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;


@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private FoodBatchRepository foodBatchRepository;

    @Override
    public void run(String... args) throws Exception {
        Product product1 = new Product();
        product1.setName("Milk");
        product1.setBarcode("59912345");
        product1.setDiscountPercent(0.0);
        productRepository.save(product1);

        FoodBatch batch1 = new FoodBatch();
        batch1.setBatchNumber("BATCH-INITIAL-01");
        batch1.setQuantity(new BigDecimal(10.0));
        batch1.setExpiryDate(LocalDate.now().plusDays(5));
        batch1.setProduct(product1);
        foodBatchRepository.save(batch1);

        Product product2 = new Product();
        product2.setName("Gouda cheese");
        product2.setBarcode("59967890");
        product2.setDiscountPercent(0.0);
        productRepository.save(product2);

        FoodBatch batch2 = new FoodBatch();
        batch2.setBatchNumber("BATCH-INITIAL-02");
        batch2.setQuantity(new BigDecimal(5.0));
        batch2.setExpiryDate(LocalDate.now().plusDays(2));
        batch2.setProduct(product2);
        foodBatchRepository.save(batch2);
    }
}
