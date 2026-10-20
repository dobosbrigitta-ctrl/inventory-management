package org.example;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByNameAndBarcodeAndExpirationDate(String name, String barcode, LocalDate expirationDate);

    @Query("SELECT p FROM Product p WHERE p.discountPercent > 0 AND p.quantity > 0")
    List<Product> findRescueProducts();
}
