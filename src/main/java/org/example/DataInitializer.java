package org.example;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final ProductRepository productRepository;

    public DataInitializer(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (productRepository.count() == 0) {
            List<Product> initialProducts = List.of(
                    new Product("Milk 1.5%", "5991234500012", LocalDate.of(2026, 11, 15), 120),
                    new Product("Gouda cheese", "5991234500029", LocalDate.of(2027, 2, 20), 45),
                    new Product("Chocolate", "5991234500036", LocalDate.of(2026, 10, 12), 30),
                    new Product("Butter", "5991234500043", LocalDate.of(2026, 12, 5), 80),
                    new Product("Bubblegum", "5991234500050", LocalDate.of(2026, 10, 10), 200)
            );

            productRepository.saveAll(initialProducts);
        }
    }
}
