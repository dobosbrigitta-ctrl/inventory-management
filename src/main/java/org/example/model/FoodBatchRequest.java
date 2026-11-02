package org.example.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class FoodBatchRequest {

    private String name;
    private String barcode;
    private BigDecimal quantity;
    private BigDecimal sellingPrice;
    private BigDecimal purchasePrice;
    private LocalDate expirationDate;

    public FoodBatchRequest() {
    }

    public FoodBatchRequest(String name, String barcode, BigDecimal quantity,
                            BigDecimal sellingPrice, BigDecimal purchasePrice,
                            LocalDate expirationDate) {
        this.name = name;
        this.barcode = barcode;
        this.quantity = quantity;
        this.sellingPrice = sellingPrice;
        this.purchasePrice = purchasePrice;
        this.expirationDate = expirationDate;
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

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(BigDecimal sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    public BigDecimal getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(BigDecimal purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }
}