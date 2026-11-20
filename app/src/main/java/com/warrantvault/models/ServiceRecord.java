package com.warrantvault.models;

public class ServiceRecord {
    private long id;
    private long productId;
    private String serviceDate;
    private String issue;
    private String serviceCenter;
    private String status; // Submitted, Under Inspection, Repairing, Completed, Replaced
    private String notes;
    private String createdAt;

    public ServiceRecord() {
    }

    public ServiceRecord(long id, long productId, String serviceDate, String issue,
                         String serviceCenter, String status, String notes, String createdAt) {
        this.id = id;
        this.productId = productId;
        this.serviceDate = serviceDate;
        this.issue = issue;
        this.serviceCenter = serviceCenter;
        this.status = status;
        this.notes = notes;
        this.createdAt = createdAt;
    }

    public ServiceRecord(long productId, String serviceDate, String issue,
                         String serviceCenter, String status, String notes, String createdAt) {
        this.productId = productId;
        this.serviceDate = serviceDate;
        this.issue = issue;
        this.serviceCenter = serviceCenter;
        this.status = status;
        this.notes = notes;
        this.createdAt = createdAt;
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

    public String getServiceDate() {
        return serviceDate;
    }

    public void setServiceDate(String serviceDate) {
        this.serviceDate = serviceDate;
    }

    public String getIssue() {
        return issue;
    }

    public void setIssue(String issue) {
        this.issue = issue;
    }

    public String getServiceCenter() {
        return serviceCenter;
    }

    public void setServiceCenter(String serviceCenter) {
        this.serviceCenter = serviceCenter;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
