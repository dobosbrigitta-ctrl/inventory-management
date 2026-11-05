package org.example.repository;

import org.example.entity.FoodBatch;
import org.example.entity.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class FoodBatchRepositoryTest {

    @Autowired
    private FoodBatchRepository foodBatchRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void findInStockBatchesByProductOrderByExpiryAsc_returnsOnlyPositiveQuantityBatchesInExpiryOrder() {
        Product product = createProduct("Milk", "12345");
        Product anotherProduct = createProduct("Bread", "67890");

        createBatch(product, "BATCH-2", 10, 10);
        createBatch(product, "BATCH-1", 5, 3);
        createBatch(product, "BATCH-3", 0, 1);
        createBatch(product, "BATCH-4", -2, 2);
        createBatch(anotherProduct, "BREAD-1", 20, 1);

        entityManager.flush();
        entityManager.clear();

        List<FoodBatch> result =
                foodBatchRepository.findInStockBatchesByProductOrderByExpiryAsc(product.getId());

        assertThat(result)
                .extracting(FoodBatch::getBatchNumber)
                .containsExactly("BATCH-1", "BATCH-2");
    }

    @Test
    void findByProductId_returnsOnlyBatchesBelongingToProduct() {
        Product firstProduct = createProduct("Milk", "12345");
        Product secondProduct = createProduct("Bread", "67890");

        createBatch(firstProduct, "MILK-1", 10, 5);
        createBatch(firstProduct, "MILK-2", 5, 3);
        createBatch(secondProduct, "BREAD-1", 8, 2);

        entityManager.flush();
        entityManager.clear();

        List<FoodBatch> result =
                foodBatchRepository.findByProductId(firstProduct.getId());

        assertThat(result)
                .extracting(FoodBatch::getBatchNumber)
                .containsExactlyInAnyOrder("MILK-1", "MILK-2");
    }

    @Test
    void deleteByProductId_deletesOnlyBatchesBelongingToProduct() {
        Product firstProduct = createProduct("Milk", "12345");
        Product secondProduct = createProduct("Bread", "67890");

        createBatch(firstProduct, "MILK-1", 10, 5);
        createBatch(firstProduct, "MILK-2", 5, 3);
        createBatch(secondProduct, "BREAD-1", 8, 2);

        entityManager.flush();

        foodBatchRepository.deleteByProductId(firstProduct.getId());

        entityManager.flush();
        entityManager.clear();

        assertThat(foodBatchRepository.findByProductId(firstProduct.getId())).isEmpty();

        assertThat(foodBatchRepository.findByProductId(secondProduct.getId()))
                .extracting(FoodBatch::getBatchNumber)
                .containsExactly("BREAD-1");
    }
    @Test
    void findUsableBatchesByProductOrderByExpiryAsc_returnsEmptyList_whenNoUsableBatchesExist() {
        Product product = createProduct("Milk", "12345");

        createBatch(product, "EXPIRED-1", 10, -3);
        createBatch(product, "EXPIRED-2", 5, -1);
        createBatch(product, "EMPTY", 0, 5);

        entityManager.flush();
        entityManager.clear();

        List<FoodBatch> result =
                foodBatchRepository.findUsableBatchesByProductOrderByExpiryAsc(product.getId());

        assertThat(result).isEmpty();
    }
    @Test
    void findUsableBatchesByProductOrderByExpiryAsc_returnsOnlyNonExpiredBatches() {
        Product product = createProduct("Milk", "12345");

        createBatch(product, "EXPIRED", 10, -1);
        createBatch(product, "EXPIRES-TODAY", 5, 0);
        createBatch(product, "FUTURE", 8, 5);
        createBatch(product, "EMPTY", 0, 3);

        entityManager.flush();
        entityManager.clear();

        List<FoodBatch> result =
                foodBatchRepository.findUsableBatchesByProductOrderByExpiryAsc(product.getId());

        assertThat(result)
                .extracting(FoodBatch::getBatchNumber)
                .containsExactly("EXPIRES-TODAY", "FUTURE");
    }

    private Product createProduct(String name, String barcode) {
        Product product = new Product();
        product.setName(name);
        product.setBarcode(barcode);
        product.setDiscountPercent(0);

        return entityManager.persist(product);
    }

    private FoodBatch createBatch(
            Product product,
            String batchNumber,
            int quantity,
            int expiryDays) {

        FoodBatch batch = new FoodBatch();
        batch.setProduct(product);
        batch.setBatchNumber(batchNumber);
        batch.setQuantity(BigDecimal.valueOf(quantity));
        batch.setExpiryDate(LocalDate.now().plusDays(expiryDays));

        return entityManager.persist(batch);
    }
}