
package org.example.controller;

import org.example.entity.FoodBatch;
import org.example.entity.Product;
import org.example.repository.ProductRepository;
import org.example.repository.FoodBatchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private FoodBatchRepository foodBatchRepository;

    @GetMapping
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<Product> createProductWithBatch(
            @RequestParam("name") String name,
            @RequestParam("barcode") String barcode,
            @RequestParam("quantity") BigDecimal quantity,
            @RequestParam("sellingPrice") BigDecimal sellingPrice,
            @RequestParam("purchasePrice") BigDecimal purchasePrice,
            @RequestParam("expirationDate") String expirationDate) {

        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0
                || sellingPrice == null || sellingPrice.compareTo(BigDecimal.ZERO) < 0
                || purchasePrice == null || purchasePrice.compareTo(BigDecimal.ZERO) < 0) {
            return ResponseEntity.badRequest().build();
        }

        System.out.println("INCOMING PRODUCT: [" + name + "] BARCODE: [" + barcode + "]");

        productRepository.findFirstByNameAndBarcodeOrderByIdAsc(name.trim(), barcode.trim())
                .ifPresentOrElse(
                        existing -> System.out.println("EXISTING PRODUCT FOUND: ID=" + existing.getId()),
                        () -> System.out.println("NO EXISTING PRODUCT FOUND")
                );

        Product product = productRepository.findFirstByNameAndBarcodeOrderByIdAsc(name.trim(), barcode.trim())
                .orElseGet(() -> {
                    Product newProd = new Product();
                    newProd.setName(name.trim());
                    newProd.setBarcode(barcode.trim());
                    newProd.setDiscountPercent(0.0);
                    return newProd;
                });

        product.setSellingPrice(sellingPrice);
        product = productRepository.save(product);

        FoodBatch batch = new FoodBatch();
        batch.setBatchNumber("BATCH-" + System.currentTimeMillis());
        batch.setQuantity(quantity);
        batch.setPurchasePrice(purchasePrice);
        batch.setExpiryDate(LocalDate.parse(expirationDate));
        batch.setProduct(product);

        foodBatchRepository.save(batch);

        return ResponseEntity.ok(product);
    }

    @PatchMapping("/batches/{batchId}")
    public ResponseEntity<FoodBatch> updateBatchQuantity(
            @PathVariable Long batchId,
            @RequestParam BigDecimal newQuantity) {

        if (newQuantity == null ||
                newQuantity.compareTo(BigDecimal.ZERO) < 0) {
            return ResponseEntity.badRequest().build();
        }

        return foodBatchRepository.findById(batchId)
                .map(batch -> {
                    batch.setQuantity(newQuantity);
                    FoodBatch updatedBatch = foodBatchRepository.save(batch);
                    return ResponseEntity.ok(updatedBatch);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        if (!productRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        foodBatchRepository.deleteByProductId(id);
        productRepository.deleteById(id);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/checkout")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<String> checkOutGoods(
            @PathVariable Long id,
            @RequestParam("requestedQuantity") BigDecimal requestedQuantity) {

        if (requestedQuantity == null ||
                requestedQuantity.compareTo(BigDecimal.ZERO) <= 0) {
            return ResponseEntity.badRequest()
                    .body("Requested quantity must be greater than 0");
        }

        List<FoodBatch> availableBatches =
                foodBatchRepository.findAvailableBatchesByProductOrderByExpiryAsc(id);

        BigDecimal totalAvailable = availableBatches.stream()
                .map(FoodBatch::getQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalAvailable.compareTo(requestedQuantity) < 0) {
            return ResponseEntity.badRequest().body(
                    "Not enough stock available! Requested: "
                            + requestedQuantity + ", Available: " + totalAvailable);
        }

        BigDecimal remainingToDeduct = requestedQuantity;

        for (FoodBatch batch : availableBatches) {

            if (remainingToDeduct.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }

            if (batch.getQuantity().compareTo(remainingToDeduct) >= 0) {
                batch.setQuantity(
                        batch.getQuantity().subtract(remainingToDeduct));
                remainingToDeduct = BigDecimal.ZERO;
            } else {
                remainingToDeduct = remainingToDeduct.subtract(batch.getQuantity());
                batch.setQuantity(BigDecimal.ZERO);
            }

            foodBatchRepository.save(batch);
        }

        return ResponseEntity.ok(
                "Successfully checked out " + requestedQuantity
                        + " units using FEFO strategy.");
    }
}
