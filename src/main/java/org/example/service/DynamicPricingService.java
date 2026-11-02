package org.example.service;

import org.example.entity.FoodBatch;
import org.example.entity.UserEntity;
import org.example.repository.FoodBatchRepository;
import org.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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
            if (batch.getExpiryDate() == null || batch.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
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
