package org.example;

public class ProductRequest {
    private String name;
    private String barcode;
    private double discountPercent;

    public ProductRequest() {
    }

    public ProductRequest(String name, String barcode, double discountPercent) {
        this.name = name;
        this.barcode = barcode;
        this.discountPercent = discountPercent;
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

    public double getDiscountPercent() {
        return discountPercent;
    }

    public void setDiscountPercent(double discountPercent) {
        this.discountPercent = discountPercent;
    }
}
