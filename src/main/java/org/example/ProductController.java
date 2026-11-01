package org.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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
            @RequestParam("quantity") double quantity,
            @RequestParam("expirationDate") String expirationDate) {

        Product product = productRepository.findByNameAndBarcode(name, barcode)
                .orElseGet(() -> {
                    Product newProd = new Product();
                    newProd.setName(name);
                    newProd.setBarcode(barcode);
                    newProd.setDiscountPercent(0.0);
                    return productRepository.save(newProd);
                });

        FoodBatch batch = new FoodBatch();
        batch.setBatchNumber("BATCH-" + System.currentTimeMillis());
        batch.setQuantity(quantity);
        batch.setExpiryDate(java.time.LocalDate.parse(expirationDate));
        batch.setProduct(product);

        foodBatchRepository.save(batch);

        return ResponseEntity.ok(product);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody ProductRequest request) {
        return productRepository.findById(id)
                .map(product -> {
                    product.setName(request.getName());
                    product.setBarcode(request.getBarcode());
                    product.setDiscountPercent(request.getDiscountPercent());
                    Product updatedProduct = productRepository.save(product);
                    return ResponseEntity.ok(updatedProduct);
                })
                .orElse(ResponseEntity.notFound().build());
    }
    @PatchMapping("/batches/{batchId}")
    public ResponseEntity<FoodBatch> updateBatchQuantity(@PathVariable Long batchId, @RequestParam double newQuantity) {
        if (newQuantity < 0) {
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

    @PostMapping("/{id}/checkout")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<String> checkOutGoods(@PathVariable Long id, @RequestParam("requestedQuantity") double requestedQuantity) {
        if (requestedQuantity <= 0) {
            return ResponseEntity.badRequest().body("Requested quantity must be greater than 0");
        }

        List<FoodBatch> availableBatches = foodBatchRepository.findAvailableBatchesByProductOrderByExpiryAsc(id);

        double totalAvailable = availableBatches.stream().mapToDouble(FoodBatch::getQuantity).sum();
        if (totalAvailable < requestedQuantity) {

            return ResponseEntity.badRequest().body("Not enough stock available! Requested: " + requestedQuantity + ", Available: " + totalAvailable);
        }

        double remainingToDeduct = requestedQuantity;

        for (FoodBatch batch : availableBatches) {
            if (remainingToDeduct <= 0) {
                break;
            }

            if (batch.getQuantity() >= remainingToDeduct) {
                batch.setQuantity(batch.getQuantity() - remainingToDeduct);
                remainingToDeduct = 0;
            } else {
                remainingToDeduct -= batch.getQuantity();
                batch.setQuantity(0);
            }

            foodBatchRepository.save(batch);
        }

        return ResponseEntity.ok("Successfully checked out " + requestedQuantity + " units using FEFO strategy.");
    }
    }


