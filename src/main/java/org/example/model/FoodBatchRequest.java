
package org.example.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class FoodBatchRequest {
    private String name;
    private String barcode;
    private BigDecimal quantity;
    private LocalDate expirationDate;

    public FoodBatchRequest() {
    }

    public FoodBatchRequest(String name, String barcode, BigDecimal quantity, LocalDate expirationDate) {
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

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }
}

