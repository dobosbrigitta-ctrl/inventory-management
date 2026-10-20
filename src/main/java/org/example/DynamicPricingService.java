package org.example;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class DynamicPricingService {

    private final ProductRepository productRepository;

    public DynamicPricingService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void updateExpiringProductPrices() {
        LocalDate today = LocalDate.now();
        List<Product> allProducts = productRepository.findAll();

        for (Product product : allProducts) {
            if (product.getExpirationDate() == null || product.getQuantity() <= 0) {
                continue;
            }

            long daysUntilExpiry = ChronoUnit.DAYS.between(today, product.getExpirationDate());

            if (daysUntilExpiry <= 0) {
                product.applyDiscount(100);
            } else if (daysUntilExpiry <= 3) {
                product.applyDiscount(50);
            } else if (daysUntilExpiry <= 7) {
                product.applyDiscount(30);
            } else {
                product.resetPrice();
            }
        }
    }
}
