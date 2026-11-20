package com.warrantvault.models;

public class Document {
    private long id;
    private long productId;
    private String documentType;
    private String filePath;
    private String createdAt;

    // Helper display fields
    private String fileName;
    private String fileSize;

    public Document() {
    }

    public Document(long id, long productId, String documentType, String filePath, String createdAt) {
        this.id = id;
        this.productId = productId;
        this.documentType = documentType;
        this.filePath = filePath;
        this.createdAt = createdAt;
    }

    public Document(long productId, String documentType, String filePath, String createdAt) {
        this.productId = productId;
        this.documentType = documentType;
        this.filePath = filePath;
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

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }
}
