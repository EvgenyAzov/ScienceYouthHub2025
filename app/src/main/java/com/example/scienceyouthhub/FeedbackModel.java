package com.example.scienceyouthhub;

import java.util.List;

public class FeedbackModel {
    private String id;
    private String activityId;
    private String userId;
    private int score;
    private String comment;
    private long timestamp;
    private List<String> photos;

    public FeedbackModel() {}

    public FeedbackModel(String id, String activityId, String userId, int score, String comment, long timestamp, List<String> photos) {
        this.id = id;
        this.activityId = activityId;
        this.userId = userId;
        this.score = score;
        this.comment = comment;
        this.timestamp = timestamp;
        this.photos = photos;
    }

    public String getId() { return id; }
    public String getActivityId() { return activityId; }
    public String getUserId() { return userId; }
    public int getScore() { return score; }
    public String getComment() { return comment; }
    public long getTimestamp() { return timestamp; }
    public List<String> getPhotos() { return photos; }

    public void setId(String id) { this.id = id; }
    public void setActivityId(String activityId) { this.activityId = activityId; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setScore(int score) { this.score = score; }
    public void setComment(String comment) { this.comment = comment; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public void setPhotos(List<String> photos) { this.photos = photos; }
}
