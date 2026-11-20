package com.warrantvault.models;

public class NotificationItem {
    private long id;
    private long productId;
    private String title;
    private String message;
    private String notificationDate;
    private boolean isRead;

    // Helper display fields
    private String productName;

    public NotificationItem() {
    }

    public NotificationItem(long id, long productId, String title, String message, String notificationDate, boolean isRead) {
        this.id = id;
        this.productId = productId;
        this.title = title;
        this.message = message;
        this.notificationDate = notificationDate;
        this.isRead = isRead;
    }

    public NotificationItem(long productId, String title, String message, String notificationDate, boolean isRead) {
        this.productId = productId;
        this.title = title;
        this.message = message;
        this.notificationDate = notificationDate;
        this.isRead = isRead;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getNotificationDate() {
        return notificationDate;
    }

    public void setNotificationDate(String notificationDate) {
        this.notificationDate = notificationDate;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }
}
