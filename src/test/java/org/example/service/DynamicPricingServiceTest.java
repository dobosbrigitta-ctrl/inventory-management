package org.example.service;

import org.example.entity.FoodBatch;
import org.example.entity.Product;
import org.example.repository.FoodBatchRepository;
import org.example.repository.ProductRepository;
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

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private DynamicPricingService dynamicPricingService;

    private Product product;
    private FoodBatch batch;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1L);

        batch = new FoodBatch();
        batch.setProduct(product);
        batch.setQuantity(new BigDecimal("10"));

        when(productRepository.findAll()).thenReturn(List.of(product));
    }

    @Test
    void updatePricing_shouldSet100PercentDiscount_whenBatchIsExpired() {
        batch.setExpiryDate(LocalDate.now().minusDays(1));
        when(foodBatchRepository.findAll()).thenReturn(List.of(batch));

        dynamicPricingService.updatePricing();

        assertEquals(100.0, product.getDiscountPercent());
        verify(productRepository).saveAll(List.of(product));
    }

    @Test
    void updatePricing_shouldSet100PercentDiscount_whenBatchExpiresToday() {
        batch.setExpiryDate(LocalDate.now());
        when(foodBatchRepository.findAll()).thenReturn(List.of(batch));

        dynamicPricingService.updatePricing();

        assertEquals(100.0, product.getDiscountPercent());
        verify(productRepository).saveAll(List.of(product));
    }

    @Test
    void updatePricing_shouldSet50PercentDiscount_whenBatchExpiresTomorrow() {
        batch.setExpiryDate(LocalDate.now().plusDays(1));
        when(foodBatchRepository.findAll()).thenReturn(List.of(batch));

        dynamicPricingService.updatePricing();

        assertEquals(50.0, product.getDiscountPercent());
        verify(productRepository).saveAll(List.of(product));
    }

    @Test
    void updatePricing_shouldSet30PercentDiscount_whenBatchExpiresWithinSevenDays() {
        batch.setExpiryDate(LocalDate.now().plusDays(5));
        when(foodBatchRepository.findAll()).thenReturn(List.of(batch));

        dynamicPricingService.updatePricing();

        assertEquals(30.0, product.getDiscountPercent());
        verify(productRepository).saveAll(List.of(product));
    }

    @Test
    void updatePricing_shouldSetZeroDiscount_whenBatchExpiresAfterSevenDays() {
        batch.setExpiryDate(LocalDate.now().plusDays(10));
        when(foodBatchRepository.findAll()).thenReturn(List.of(batch));

        dynamicPricingService.updatePricing();

        assertEquals(0.0, product.getDiscountPercent());
        verify(productRepository).saveAll(List.of(product));
    }

    @Test
    void updatePricing_shouldSetZeroDiscount_whenExpiryDateIsNull() {
        batch.setExpiryDate(null);
        when(foodBatchRepository.findAll()).thenReturn(List.of(batch));

        dynamicPricingService.updatePricing();

        assertEquals(0.0, product.getDiscountPercent());
        verify(productRepository).saveAll(List.of(product));
    }

    @Test
    void updatePricing_shouldSetZeroDiscount_whenQuantityIsZero() {
        batch.setExpiryDate(LocalDate.now().plusDays(2));
        batch.setQuantity(BigDecimal.ZERO);
        when(foodBatchRepository.findAll()).thenReturn(List.of(batch));

        dynamicPricingService.updatePricing();

        assertEquals(0.0, product.getDiscountPercent());
        verify(productRepository).saveAll(List.of(product));
    }

    @Test
    void updatePricing_shouldSetZeroDiscount_whenQuantityIsNegative() {
        batch.setExpiryDate(LocalDate.now().plusDays(2));
        batch.setQuantity(new BigDecimal("-1"));
        when(foodBatchRepository.findAll()).thenReturn(List.of(batch));

        dynamicPricingService.updatePricing();

        assertEquals(0.0, product.getDiscountPercent());
        verify(productRepository).saveAll(List.of(product));
    }

    @Test
    void updatePricing_shouldSet30PercentDiscount_whenBatchExpiresInSevenDays() {
        batch.setExpiryDate(LocalDate.now().plusDays(7));
        when(foodBatchRepository.findAll()).thenReturn(List.of(batch));

        dynamicPricingService.updatePricing();

        assertEquals(30.0, product.getDiscountPercent());
        verify(productRepository).saveAll(List.of(product));
    }

    @Test
    void updatePricing_shouldSetZeroDiscount_whenQuantityIsNull() {
        batch.setExpiryDate(LocalDate.now().plusDays(2));
        batch.setQuantity(null);
        when(foodBatchRepository.findAll()).thenReturn(List.of(batch));

        dynamicPricingService.updatePricing();

        assertEquals(0.0, product.getDiscountPercent());
        verify(productRepository).saveAll(List.of(product));
    }

    @Test
    void updatePricing_shouldUseEarliestExpiryDate_whenProductHasMultipleBatches() {
        FoodBatch laterBatch = new FoodBatch();
        laterBatch.setProduct(product);
        laterBatch.setExpiryDate(LocalDate.now().plusDays(10));
        laterBatch.setQuantity(new BigDecimal("5"));

        FoodBatch earliestBatch = new FoodBatch();
        earliestBatch.setProduct(product);
        earliestBatch.setExpiryDate(LocalDate.now().plusDays(2));
        earliestBatch.setQuantity(new BigDecimal("10"));

        FoodBatch latestBatch = new FoodBatch();
        latestBatch.setProduct(product);
        latestBatch.setExpiryDate(LocalDate.now().plusDays(15));
        latestBatch.setQuantity(new BigDecimal("20"));

        when(foodBatchRepository.findAll())
                .thenReturn(List.of(laterBatch, earliestBatch, latestBatch));

        dynamicPricingService.updatePricing();

        assertEquals(30.0, product.getDiscountPercent());
        verify(productRepository).saveAll(List.of(product));
    }

    @Test
    void updatePricing_shouldResetDiscountToZero_whenNoValidBatchExists() {
        product.setDiscountPercent(50.0);

        when(foodBatchRepository.findAll()).thenReturn(List.of());

        dynamicPricingService.updatePricing();

        assertEquals(0.0, product.getDiscountPercent());
        verify(productRepository).saveAll(List.of(product));
    }

    @Test
    void updatePricing_shouldSetZeroDiscount_whenProductIdIsNull() {
        product.setId(null);
        product.setDiscountPercent(50.0);

        batch.setExpiryDate(LocalDate.now().plusDays(1));

        when(foodBatchRepository.findAll()).thenReturn(List.of(batch));

        dynamicPricingService.updatePricing();

        assertEquals(0.0, product.getDiscountPercent());
        verify(productRepository).saveAll(List.of(product));
    }

    @Test
    void updatePricing_shouldNotSaveAnything_whenNoProductsExist() {
        when(productRepository.findAll()).thenReturn(List.of());
        when(foodBatchRepository.findAll()).thenReturn(List.of());

        dynamicPricingService.updatePricing();

        verify(productRepository).saveAll(List.of());
    }

    @Test
    void updatePricing_shouldIgnoreBatch_whenBatchHasNoProduct() {
        batch.setProduct(null);
        batch.setExpiryDate(LocalDate.now().plusDays(1));

        when(foodBatchRepository.findAll()).thenReturn(List.of(batch));

        dynamicPricingService.updatePricing();

        assertEquals(0.0, product.getDiscountPercent());
        verify(productRepository).saveAll(List.of(product));
    }

    @Test
    void updatePricing_shouldCalculateDiscountsIndependently_forMultipleProducts() {
        Product secondProduct = new Product();
        secondProduct.setId(2L);

        FoodBatch secondBatch = new FoodBatch();
        secondBatch.setProduct(secondProduct);
        secondBatch.setQuantity(new BigDecimal("5"));
        secondBatch.setExpiryDate(LocalDate.now().plusDays(10));

        batch.setExpiryDate(LocalDate.now().plusDays(1));

        when(productRepository.findAll()).thenReturn(List.of(product, secondProduct));
        when(foodBatchRepository.findAll()).thenReturn(List.of(batch, secondBatch));

        dynamicPricingService.updatePricing();

        assertEquals(50.0, product.getDiscountPercent());
        assertEquals(0.0, secondProduct.getDiscountPercent());

        verify(productRepository).saveAll(List.of(product, secondProduct));
    }
}