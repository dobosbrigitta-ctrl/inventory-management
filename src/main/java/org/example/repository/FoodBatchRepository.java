package org.example.repository;

import org.example.entity.FoodBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FoodBatchRepository extends JpaRepository<FoodBatch, Long> {

    @Query("SELECT b FROM FoodBatch b WHERE b.product.id = :productId AND b.quantity > 0 ORDER BY b.expiryDate ASC")
    List<FoodBatch> findInStockBatchesByProductOrderByExpiryAsc(@Param("productId") Long productId);

    @Query("SELECT b FROM FoodBatch b WHERE b.product.id = :productId AND b.quantity > 0 AND b.expiryDate >= CURRENT_DATE ORDER BY b.expiryDate ASC")
    List<FoodBatch> findUsableBatchesByProductOrderByExpiryAsc(@Param("productId") Long productId);

    List<FoodBatch> findByProductId(Long productId);

    void deleteByProductId(Long productId);
}