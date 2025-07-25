package com.example.scienceyouthhub;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class FeedbackModel implements Serializable {
    // Основные поля
    private String id; // Firestore docId (универсально)
    private String feedbackId; // Старое поле для обратной совместимости (можно оставить)
    private String activityId;
    private String activityName;

    // Кто оставил отзыв
    private String userId;     // Для старого кода
    private String userName;   // Для старого кода
    private String authorId;   // Новый стиль
    private String authorName; // Новый стиль

    private String comment;
    private int rating;

    private Date date;

    // Для ответов на отзывы (опционально)
    private String parentFeedbackId;

    // Для фото в отзыве (опционально)
    private List<String> photoBase64List;

    public FeedbackModel() {}

    // Новый конструктор с максимальным числом полей
    public FeedbackModel(String id, String feedbackId, String activityId, String activityName,
                         String userId, String userName, String authorId, String authorName,
                         String comment, int rating, Date date,
                         String parentFeedbackId, List<String> photoBase64List) {
        this.id = id;
        this.feedbackId = feedbackId;
        this.activityId = activityId;
        this.activityName = activityName;
        this.userId = userId;
        this.userName = userName;
        this.authorId = authorId;
        this.authorName = authorName;
        this.comment = comment;
        this.rating = rating;
        this.date = date;
        this.parentFeedbackId = parentFeedbackId;
        this.photoBase64List = photoBase64List;
    }

    // Минимальный конструктор (старый стиль)
    public FeedbackModel(String feedbackId, String activityId, String activityName,
                         String userId, String userName, String comment, int rating) {
        this.feedbackId = feedbackId;
        this.activityId = activityId;
        this.activityName = activityName;
        this.userId = userId;
        this.userName = userName;
        this.comment = comment;
        this.rating = rating;
    }

    // Getters/Setters для всех полей
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFeedbackId() { return feedbackId; }
    public void setFeedbackId(String feedbackId) { this.feedbackId = feedbackId; }

    public String getActivityId() { return activityId; }
    public void setActivityId(String activityId) { this.activityId = activityId; }

    public String getActivityName() { return activityName; }
    public void setActivityName(String activityName) { this.activityName = activityName; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getAuthorId() { return authorId; }
    public void setAuthorId(String authorId) { this.authorId = authorId; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }

    public String getParentFeedbackId() { return parentFeedbackId; }
    public void setParentFeedbackId(String parentFeedbackId) { this.parentFeedbackId = parentFeedbackId; }

    public List<String> getPhotoBase64List() { return photoBase64List; }
    public void setPhotoBase64List(List<String> photoBase64List) { this.photoBase64List = photoBase64List; }
}
