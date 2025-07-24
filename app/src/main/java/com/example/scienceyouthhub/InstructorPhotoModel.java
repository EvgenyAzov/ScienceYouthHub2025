package com.example.scienceyouthhub;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class InstructorPhotoModel {
    private String id;
    private String activityId;
    private String instructorId;
    private Date date;
    private String imageBase64;

    public InstructorPhotoModel() {}

    public InstructorPhotoModel(String id, String activityId, String instructorId, Date date, String imageBase64) {
        this.id = id;
        this.activityId = activityId;
        this.instructorId = instructorId;
        this.date = date;
        this.imageBase64 = imageBase64;
    }

    public String getId() { return id; }
    public String getActivityId() { return activityId; }
    public String getInstructorId() { return instructorId; }
    public Date getDate() { return date; }
    public String getImageBase64() { return imageBase64; }
    public String getDateString() {
        if (date == null) return "";
        return new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(date);
    }

    public void setId(String id) { this.id = id; }
    public void setActivityId(String activityId) { this.activityId = activityId; }
    public void setInstructorId(String instructorId) { this.instructorId = instructorId; }
    public void setDate(Date date) { this.date = date; }
    public void setImageBase64(String imageBase64) { this.imageBase64 = imageBase64; }
}
