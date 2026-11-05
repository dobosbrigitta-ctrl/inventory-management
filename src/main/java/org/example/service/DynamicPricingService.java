
package org.example.service;

import org.example.entity.FoodBatch;
import org.example.entity.Product;
import org.example.repository.FoodBatchRepository;
import org.example.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DynamicPricingService {

    private final FoodBatchRepository foodBatchRepository;
    private final ProductRepository productRepository;

    public DynamicPricingService(
            FoodBatchRepository foodBatchRepository,
            ProductRepository productRepository) {
        this.foodBatchRepository = foodBatchRepository;
        this.productRepository = productRepository;
    }

    public void updatePricing() {
        LocalDate today = LocalDate.now();

        List<Product> products = productRepository.findAll();
        List<FoodBatch> allBatches = foodBatchRepository.findAll();

        Map<Long, LocalDate> earliestExpiryDates = new HashMap<>();

        for (FoodBatch batch : allBatches) {
            if (batch.getProduct() == null
                    || batch.getProduct().getId() == null
                    || batch.getExpiryDate() == null
                    || batch.getQuantity() == null
                    || batch.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            Long productId = batch.getProduct().getId();

            earliestExpiryDates.merge(
                    productId,
                    batch.getExpiryDate(),
                    (existing, current) ->
                            existing.isBefore(current) ? existing : current
            );
        }

        for (Product product : products) {
            LocalDate earliestExpiry = earliestExpiryDates.get(product.getId());

            double discount = 0;

            if (earliestExpiry != null) {
                long daysUntilExpiry =
                        ChronoUnit.DAYS.between(today, earliestExpiry);

                if (daysUntilExpiry <= 0) {
                    discount = 100;
                } else if (daysUntilExpiry <= 1) {
                    discount = 50;
                } else if (daysUntilExpiry <= 7) {
                    discount = 30;
                }
            }

            product.setDiscountPercent(discount);
        }

        productRepository.saveAll(products);
    }
}
