package com.example.scienceyouthhub;

import java.io.Serializable;

public class FeedbackModel implements Serializable {
    private String feedbackId;
    private String activityId;
    private String activityName;
    private String userId;
    private String userName;
    private String comment;
    private int rating;
    // Если хочешь — можно добавить List<String> photoBase64 для фото в отзыве

    public FeedbackModel() {}

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

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }
}
