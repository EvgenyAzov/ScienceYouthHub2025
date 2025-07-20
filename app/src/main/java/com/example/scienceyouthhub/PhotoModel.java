package com.example.scienceyouthhub;

import java.io.Serializable;

public class PhotoModel implements Serializable {
    private String photoId;
    private String photoBase64;
    private String activityId;
    private String activityName;
    private String userId;
    private String userName;

    public PhotoModel() {}

    public PhotoModel(String photoId, String photoBase64, String activityId, String activityName, String userId, String userName) {
        this.photoId = photoId;
        this.photoBase64 = photoBase64;
        this.activityId = activityId;
        this.activityName = activityName;
        this.userId = userId;
        this.userName = userName;
    }

    public String getPhotoId() { return photoId; }
    public void setPhotoId(String photoId) { this.photoId = photoId; }

    public String getPhotoBase64() { return photoBase64; }
    public void setPhotoBase64(String photoBase64) { this.photoBase64 = photoBase64; }

    public String getActivityId() { return activityId; }
    public void setActivityId(String activityId) { this.activityId = activityId; }

    public String getActivityName() { return activityName; }
    public void setActivityName(String activityName) { this.activityName = activityName; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
}
