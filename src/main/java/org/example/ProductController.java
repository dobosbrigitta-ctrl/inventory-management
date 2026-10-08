package org.example;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
public class ProductController {

    private final ProductRepository productRepository;

    public ProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @GetMapping("/products")
    public List<Product> getProducts() {
        return productRepository.findAll();
    }

    @GetMapping("/products/add")
    public String addProduct(@RequestParam String name, @RequestParam String barcode, @RequestParam String expirationDate, @RequestParam int quantity) {
        LocalDate parsedDate = LocalDate.parse(expirationDate);


        Optional<Product> existingProductOpt = productRepository.findByNameAndBarcodeAndExpirationDate(name, barcode, parsedDate);

        if (existingProductOpt.isPresent()) {

            Product existingProduct = existingProductOpt.get();
            existingProduct.setQuantity(existingProduct.getQuantity() + quantity);
            productRepository.save(existingProduct);
            return "Quantity updated. New total value to this value: " + existingProduct.getQuantity();
        } else {

            Product newProduct = new Product(name, barcode, parsedDate, quantity);
            productRepository.save(newProduct);
            return "New product successfully saved to PostgreSQL: " + name;
        }
    }
}
