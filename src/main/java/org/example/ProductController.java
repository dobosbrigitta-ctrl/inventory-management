package org.example;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class ProductController {

    private final ProductRepository productRepository;
    private final DynamicPricingService dynamicPricingService;

    public ProductController(ProductRepository productRepository, DynamicPricingService dynamicPricingService) {
        this.productRepository = productRepository;
        this.dynamicPricingService = dynamicPricingService;
    }

    @GetMapping("/products")
    public List<Product> getProducts() {
        return productRepository.findAll();
    }

    @GetMapping("/products/stats")
    public DashboardStats getDashboardStats() {
        List<Product> allProducts = productRepository.findAll();

        long standardCount = allProducts.stream()
                .filter(p -> p.getDiscountPercent() == 0)
                .mapToLong(Product::getQuantity)
                .sum();

        long rescueCount = allProducts.stream()
                .filter(p -> p.getDiscountPercent() > 0)
                .mapToLong(Product::getQuantity)
                .sum();

        Map<String, Integer> criticalItems = allProducts.stream()
                .filter(p -> p.getQuantity() <= 5 && p.getQuantity() > 0)
                .collect(Collectors.toMap(
                        Product::getName,
                        Product::getQuantity,
                        (existing, replacement) -> existing + replacement
                ));

        return new DashboardStats(standardCount, rescueCount, criticalItems);
    }

    @GetMapping("/products/add")
    public String addProduct(@RequestParam String name, @RequestParam String barcode, @RequestParam String expirationDate, @RequestParam int quantity, @RequestParam int originalPrice) {
        LocalDate parsedDate = LocalDate.parse(expirationDate);
        Optional<Product> existingProductOpt = productRepository.findByNameAndBarcodeAndExpirationDate(name, barcode, parsedDate);

        if (existingProductOpt.isPresent()) {
            Product existingProduct = existingProductOpt.get();
            existingProduct.setQuantity(existingProduct.getQuantity() + quantity);
            productRepository.save(existingProduct);
            return "Updated";
        } else {
            Product newProduct = new Product(name, barcode, parsedDate, quantity, originalPrice);
            productRepository.save(newProduct);
            return "Saved";
        }
    }

    @GetMapping("/products/trigger-pricing")
    public String triggerPricing() {
        dynamicPricingService.updateExpiringProductPrices();
        return "Prices have been updated";
    }

    @GetMapping("/products/update-stock")
    public ResponseEntity<String> updateStock(
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String barcode,
            @RequestParam int amount,
            @RequestParam String mode) {

        if (id != null) {
            Optional<Product> prodOpt = productRepository.findById(id);
            if (prodOpt.isPresent()) {
                Product p = prodOpt.get();
                if (mode.equalsIgnoreCase("reduce")) {
                    if (p.getQuantity() < amount) return ResponseEntity.badRequest().body("Low stock");
                    p.setQuantity(p.getQuantity() - amount);
                } else {
                    p.setQuantity(p.getQuantity() + amount);
                }
                productRepository.save(p);
                return ResponseEntity.ok("Updated");
            }
        } else if (name != null && barcode != null) {
            List<Product> products = productRepository.findAll().stream()
                    .filter(p -> p.getName().equalsIgnoreCase(name) && p.getBarcode().equals(barcode))
                    .sorted(Comparator.comparing(Product::getExpirationDate))
                    .collect(Collectors.toList());

            if (products.isEmpty()) return ResponseEntity.badRequest().body("Not found");

            if (mode.equalsIgnoreCase("reduce")) {
                int remainingToReduce = amount;
                for (Product p : products) {
                    if (remainingToReduce <= 0) break;
                    int currentQty = p.getQuantity();
                    if (currentQty <= remainingToReduce) {
                        remainingToReduce -= currentQty;
                        p.setQuantity(0);
                        productRepository.save(p);
                    } else {
                        p.setQuantity(currentQty - remainingToReduce);
                        remainingToReduce = 0;
                        productRepository.save(p);
                    }
                }
                if (remainingToReduce > 0) return ResponseEntity.badRequest().body("Low stock");
            } else {
                Product p = products.get(products.size() - 1);
                p.setQuantity(p.getQuantity() + amount);
                productRepository.save(p);
            }
            return ResponseEntity.ok("Updated group");
        }
        return ResponseEntity.badRequest().body("Error");
    }

    @GetMapping("/products/delete")
    public ResponseEntity<String> deleteProduct(
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String barcode) {

        if (id != null) {
            productRepository.deleteById(id);
            return ResponseEntity.ok("Deleted");
        } else if (name != null && barcode != null) {
            List<Product> products = productRepository.findAll().stream()
                    .filter(p -> p.getName().equalsIgnoreCase(name) && p.getBarcode().equals(barcode))
                    .collect(Collectors.toList());
            productRepository.deleteAll(products);
            return ResponseEntity.ok("Deleted group");
        }
        return ResponseEntity.badRequest().body("Error");
    }
    @GetMapping("/products/update-price")
    public ResponseEntity<String> updateProductDetails(
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) String originalName,
            @RequestParam(required = false) String barcode,
            @RequestParam String name,
            @RequestParam int price,
            @RequestParam int discount) {

        int currentPrice = (int) Math.round(price * (1.0 - (discount / 100.0)));

        if (id != null) {
            Optional<Product> prodOpt = productRepository.findById(id);
            if (prodOpt.isPresent()) {
                Product p = prodOpt.get();
                p.setName(name);
                p.setOriginalPrice(price);
                p.setDiscountPercent(discount);
                p.setCurrentPrice(currentPrice);
                productRepository.save(p);
                return ResponseEntity.ok("Updated");
            }
        } else if (originalName != null && barcode != null) {
            List<Product> products = productRepository.findAll().stream()
                    .filter(p -> p.getName().equalsIgnoreCase(originalName) && p.getBarcode().equals(barcode))
                    .collect(Collectors.toList());

            if (products.isEmpty()) return ResponseEntity.badRequest().body("Not found");

            for (Product p : products) {
                p.setName(name);
                p.setOriginalPrice(price);
                p.setDiscountPercent(discount);
                p.setCurrentPrice(currentPrice);
                productRepository.save(p);
            }
            return ResponseEntity.ok("Updated group");
        }
        return ResponseEntity.badRequest().body("Error");
    }

}
