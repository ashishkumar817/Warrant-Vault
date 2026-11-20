package com.warrantvault.models;

public class Warranty {
    private long id;
    private long productId;
    private String startDate;
    private String endDate;
    private String warrantyPeriod;
    private String provider;
    private String status;

    public Warranty() {
    }

    public Warranty(long id, long productId, String startDate, String endDate, String warrantyPeriod, String provider, String status) {
        this.id = id;
        this.productId = productId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.warrantyPeriod = warrantyPeriod;
        this.provider = provider;
        this.status = status;
    }

    public Warranty(long productId, String startDate, String endDate, String warrantyPeriod, String provider, String status) {
        this.productId = productId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.warrantyPeriod = warrantyPeriod;
        this.provider = provider;
        this.status = status;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getProductId() {
        return productId;
    }

    public void setProductId(long productId) {
        this.productId = productId;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getWarrantyPeriod() {
        return warrantyPeriod;
    }

    public void setWarrantyPeriod(String warrantyPeriod) {
        this.warrantyPeriod = warrantyPeriod;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
