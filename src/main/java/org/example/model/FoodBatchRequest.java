package org.example.model;

import java.time.LocalDate;

public class FoodBatchRequest {
    private String name;
    private String barcode;
    private double quantity;
    private LocalDate expirationDate;

    public FoodBatchRequest() {
    }

    public FoodBatchRequest(String name, String barcode, double quantity, LocalDate expirationDate) {
        this.name = name;
        this.barcode = barcode;
        this.quantity = quantity;
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

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }
}
