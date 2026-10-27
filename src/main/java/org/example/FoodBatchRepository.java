package org.example;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FoodBatchRepository extends JpaRepository<FoodBatch, Long> {

    @Query("SELECT b FROM FoodBatch b WHERE b.product.id = :productId AND b.quantity > 0 ORDER BY b.expiryDate ASC")
    List<FoodBatch> findAvailableBatchesByProductOrderByExpiryAsc(Long productId);
}
