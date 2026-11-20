package com.warrantvault.models;

public class Product {
    private long id;
    private long userId;
    private String name;
    private String brand;
    private String category;
    private String modelNumber;
    private String serialNumber;
    private String purchaseDate;
    private double purchasePrice;
    private String seller;
    private String invoiceNumber;
    private String imagePath;
    private String createdAt;

    // Associated objects & computed attributes
    private Warranty warranty;
    private long daysRemaining;
    private int elapsedPercentage;
    private String status;

    public Product() {
    }

    public Product(long id, long userId, String name, String brand, String category,
                   String modelNumber, String serialNumber, String purchaseDate,
                   double purchasePrice, String seller, String invoiceNumber,
                   String imagePath, String createdAt) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.brand = brand;
        this.category = category;
        this.modelNumber = modelNumber;
        this.serialNumber = serialNumber;
        this.purchaseDate = purchaseDate;
        this.purchasePrice = purchasePrice;
        this.seller = seller;
        this.invoiceNumber = invoiceNumber;
        this.imagePath = imagePath;
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getModelNumber() {
        return modelNumber;
    }

    public void setModelNumber(String modelNumber) {
        this.modelNumber = modelNumber;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(String purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public double getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(double purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public String getSeller() {
        return seller;
    }

    public void setSeller(String seller) {
        this.seller = seller;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public Warranty getWarranty() {
        return warranty;
    }

    public void setWarranty(Warranty warranty) {
        this.warranty = warranty;
    }

    public long getDaysRemaining() {
        return daysRemaining;
    }

    public void setDaysRemaining(long daysRemaining) {
        this.daysRemaining = daysRemaining;
    }

    public int getElapsedPercentage() {
        return elapsedPercentage;
    }

    public void setElapsedPercentage(int elapsedPercentage) {
        this.elapsedPercentage = elapsedPercentage;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
