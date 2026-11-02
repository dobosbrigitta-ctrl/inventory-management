package org.example.repository;

import org.example.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findFirstByNameAndBarcodeOrderByIdAsc(String name, String barcode);

    @Query("SELECT DISTINCT p FROM Product p JOIN p.batches b WHERE p.discountPercent > 0 AND b.quantity > 0")
    List<Product> findRescueProducts();
}
