package org.example.service;

import org.example.entity.FoodBatch;
import org.example.entity.Product;
import org.example.repository.FoodBatchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DynamicPricingServiceTest {

    @Mock
    private FoodBatchRepository foodBatchRepository;

    @InjectMocks
    private DynamicPricingService dynamicPricingService;

    private Product product;
    private FoodBatch batch;

    @BeforeEach
    void setUp() {
        product = new Product();

        batch = new FoodBatch();
        batch.setProduct(product);
        batch.setQuantity(new BigDecimal("10"));
    }

    @Test
    void updatePricing_shouldSet100PercentDiscount_whenBatchIsExpired() {
        batch.setExpiryDate(LocalDate.now().minusDays(1));
        when(foodBatchRepository.findAll()).thenReturn(List.of(batch));

        dynamicPricingService.updatePricing();

        assertEquals(100.0, product.getDiscountPercent());
        verify(foodBatchRepository).save(batch);
    }

    @Test
    void updatePricing_shouldSet100PercentDiscount_whenBatchExpiresToday() {
        batch.setExpiryDate(LocalDate.now());
        when(foodBatchRepository.findAll()).thenReturn(List.of(batch));

        dynamicPricingService.updatePricing();

        assertEquals(100.0, product.getDiscountPercent());
        verify(foodBatchRepository).save(batch);
    }

    @Test
    void updatePricing_shouldSet50PercentDiscount_whenBatchExpiresTomorrow() {
        batch.setExpiryDate(LocalDate.now().plusDays(1));
        when(foodBatchRepository.findAll()).thenReturn(List.of(batch));

        dynamicPricingService.updatePricing();

        assertEquals(50.0, product.getDiscountPercent());
        verify(foodBatchRepository).save(batch);
    }

    @Test
    void updatePricing_shouldSet30PercentDiscount_whenBatchExpiresWithinSevenDays() {
        batch.setExpiryDate(LocalDate.now().plusDays(5));
        when(foodBatchRepository.findAll()).thenReturn(List.of(batch));

        dynamicPricingService.updatePricing();

        assertEquals(30.0, product.getDiscountPercent());
        verify(foodBatchRepository).save(batch);
    }

    @Test
    void updatePricing_shouldSetZeroDiscount_whenBatchExpiresAfterSevenDays() {
        batch.setExpiryDate(LocalDate.now().plusDays(10));
        when(foodBatchRepository.findAll()).thenReturn(List.of(batch));

        dynamicPricingService.updatePricing();

        assertEquals(0.0, product.getDiscountPercent());
        verify(foodBatchRepository).save(batch);
    }

    @Test
    void updatePricing_shouldNotSaveBatch_whenExpiryDateIsNull() {
        batch.setExpiryDate(null);
        when(foodBatchRepository.findAll()).thenReturn(List.of(batch));

        dynamicPricingService.updatePricing();

        verify(foodBatchRepository, never()).save(any());
    }

    @Test
    void updatePricing_shouldNotSaveBatch_whenQuantityIsZero() {
        batch.setExpiryDate(LocalDate.now().plusDays(2));
        batch.setQuantity(BigDecimal.ZERO);
        when(foodBatchRepository.findAll()).thenReturn(List.of(batch));

        dynamicPricingService.updatePricing();

        verify(foodBatchRepository, never()).save(any());
    }

    @Test
    void updatePricing_shouldNotSaveBatch_whenQuantityIsNegative() {
        batch.setExpiryDate(LocalDate.now().plusDays(2));
        batch.setQuantity(new BigDecimal("-1"));
        when(foodBatchRepository.findAll()).thenReturn(List.of(batch));

        dynamicPricingService.updatePricing();

        verify(foodBatchRepository, never()).save(any());
    }
}