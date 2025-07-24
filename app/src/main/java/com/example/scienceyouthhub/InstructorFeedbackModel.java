package com.example.scienceyouthhub;

import java.util.Date;

public class InstructorFeedbackModel {
    private String id;
    private String activityId;
    private String instructorId;
    private String studentId;
    private Date date;
    private String comment;
    private int score; // 1-10

    public InstructorFeedbackModel() {}

    public InstructorFeedbackModel(String id, String activityId, String instructorId, String studentId, Date date, String comment, int score) {
        this.id = id;
        this.activityId = activityId;
        this.instructorId = instructorId;
        this.studentId = studentId;
        this.date = date;
        this.comment = comment;
        this.score = score;
    }

    public String getId() { return id; }
    public String getActivityId() { return activityId; }
    public String getInstructorId() { return instructorId; }
    public String getStudentId() { return studentId; }
    public Date getDate() { return date; }
    public String getComment() { return comment; }
    public int getScore() { return score; }

    public void setId(String id) { this.id = id; }
    public void setActivityId(String activityId) { this.activityId = activityId; }
    public void setInstructorId(String instructorId) { this.instructorId = instructorId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public void setDate(Date date) { this.date = date; }
    public void setComment(String comment) { this.comment = comment; }
    public void setScore(int score) { this.score = score; }
}
