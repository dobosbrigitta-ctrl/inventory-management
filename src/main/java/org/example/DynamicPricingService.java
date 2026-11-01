package org.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class DynamicPricingService {

    @Autowired
    private FoodBatchRepository foodBatchRepository;

    public void updatePricing() {
        LocalDate today = LocalDate.now();
        List<FoodBatch> allBatches = foodBatchRepository.findAll();

        for (FoodBatch batch : allBatches) {
            if (batch.getExpiryDate() == null || batch.getQuantity() <= 0) {
                continue;
            }

            long daysUntilExpiry = ChronoUnit.DAYS.between(today, batch.getExpiryDate());

            if (daysUntilExpiry <= 0) {
                batch.getProduct().setDiscountPercent(100);
            } else if (daysUntilExpiry <= 1) {
                batch.getProduct().setDiscountPercent(50);
            } else if (daysUntilExpiry <= 7) {
                batch.getProduct().setDiscountPercent(30);
            } else {
                batch.getProduct().setDiscountPercent(0);
            }

            foodBatchRepository.save(batch);
        }
    }
}
