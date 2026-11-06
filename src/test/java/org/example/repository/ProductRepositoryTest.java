
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void findFirstByNameAndBarcodeOrderByIdAsc_returnsProductWithLowestId() {
        Product firstProduct = createProduct("Milk", "12345", 10);
        Product secondProduct = createProduct("Milk", "12345", 20);

        entityManager.flush();
        entityManager.clear();

        Optional<Product> result =
                productRepository.findFirstByNameAndBarcodeOrderByIdAsc(
                        "Milk", "12345"
                );

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(firstProduct.getId());
        assertThat(result.get().getId()).isLessThan(secondProduct.getId());
    }

    @Test
    void findRescueProducts_returnsOnlyDiscountedProductsWithAvailableBatches() {
        Product rescueProduct = createProduct("Rescue milk", "111", 30);
        Product anotherRescueProduct = createProduct("Rescue bread", "222", 20);
        Product noDiscountProduct = createProduct("Regular milk", "333", 0);
        Product noStockProduct = createProduct("Empty product", "444", 15);
        Product expiredOnlyProduct = createProduct("Expired milk", "555", 25);

        createBatch(rescueProduct, "BATCH-1", "5");
        createBatch(rescueProduct, "BATCH-2", "3");

        createBatch(anotherRescueProduct, "BATCH-3", "2");

        createBatch(noDiscountProduct, "BATCH-4", "5");

        createBatch(noStockProduct, "BATCH-5", "0");

        createBatch(expiredOnlyProduct, "EXPIRED-1", "5", -1);

        entityManager.flush();
        entityManager.clear();

        List<Product> result = productRepository.findRescueProducts();

        assertThat(result)
                .extracting(Product::getName)
                .containsExactlyInAnyOrder("Rescue milk", "Rescue bread");

        assertThat(result)
                .extracting(Product::getName)
                .doesNotHaveDuplicates();
    }

    private Product createProduct(String name, String barcode, double discountPercent) {
        Product product = new Product();
        product.setName(name);
        product.setBarcode(barcode);
        product.setDiscountPercent(discountPercent);

        return entityManager.persist(product);
    }

    private FoodBatch createBatch(
            Product product,
            String batchNumber,
            String quantity) {

        return createBatch(product, batchNumber, quantity, 3);
    }

    private FoodBatch createBatch(
            Product product,
            String batchNumber,
            String quantity,
            int expiryDays) {

        FoodBatch batch = new FoodBatch();
        batch.setProduct(product);
        batch.setBatchNumber(batchNumber);
        batch.setExpiryDate(LocalDate.now().plusDays(expiryDays));
        batch.setQuantity(new BigDecimal(quantity));

        return entityManager.persist(batch);
    }
}

