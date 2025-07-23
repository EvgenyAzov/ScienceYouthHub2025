package com.example.scienceyouthhub;

public class NotificationModel {
    private String userId;
    private String title;
    private String message;
    private long timestamp;

    public NotificationModel() {}

    public NotificationModel(String userId, String title, String message, long timestamp) {
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getUserId() { return userId; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public long getTimestamp() { return timestamp; }

    public void setUserId(String userId) { this.userId = userId; }
    public void setTitle(String title) { this.title = title; }
    public void setMessage(String message) { this.message = message; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
