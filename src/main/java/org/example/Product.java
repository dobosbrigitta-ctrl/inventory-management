package org.example;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDate;

@Entity
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String barcode;
    private LocalDate expirationDate;
    private int quantity;

    private double originalPrice;
    private double currentPrice;
    private int discountPercent = 0;

    public Product() {
    }

    public Product(String name, String barcode, LocalDate expirationDate, int quantity, double originalPrice) {
        this.name = name;
        this.barcode = barcode;
        this.expirationDate = expirationDate;
        this.quantity = quantity;
        this.originalPrice = originalPrice;
        this.currentPrice = originalPrice;
    }

    public Product(String name, String barcode, LocalDate expirationDate, int quantity) {
        this.name = name;
        this.barcode = barcode;
        this.expirationDate = expirationDate;
        this.quantity = quantity;
        this.originalPrice = 0.0;
        this.currentPrice = 0.0;
    }

    public void applyDiscount(int percent) {
        this.discountPercent = percent;
        this.currentPrice = this.originalPrice * (1.0 - (percent / 100.0));
    }

    public void resetPrice() {
        this.discountPercent = 0;
        this.currentPrice = this.originalPrice;
    }

    public double getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(double originalPrice) {
        this.originalPrice = originalPrice;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(double currentPrice) {
        this.currentPrice = currentPrice;
    }

    public int getDiscountPercent() {
        return discountPercent;
    }

    public void setDiscountPercent(int discountPercent) {
        this.discountPercent = discountPercent;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
