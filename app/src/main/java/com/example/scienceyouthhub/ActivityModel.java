package com.example.scienceyouthhub;

public class ActivityModel {
    private String id;
    private String name;
    private String category;
    private String ageRange;
    private String description;
    private String days;
    private int maxParticipants;
    private String instructorId;
    private String instructorName; // Для отображения

    public ActivityModel() {}

    public ActivityModel(String id, String name, String category, String ageRange, String description,
                         String days, int maxParticipants, String instructorId, String instructorName) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.ageRange = ageRange;
        this.description = description;
        this.days = days;
        this.maxParticipants = maxParticipants;
        this.instructorId = instructorId;
        this.instructorName = instructorName;
    }

    // Геттеры и сеттеры (можно сгенерировать)
    public String getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public String getAgeRange() { return ageRange; }
    public String getDescription() { return description; }
    public String getDays() { return days; }
    public int getMaxParticipants() { return maxParticipants; }
    public String getInstructorId() { return instructorId; }
    public String getInstructorName() { return instructorName; }

    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setCategory(String category) { this.category = category; }
    public void setAgeRange(String ageRange) { this.ageRange = ageRange; }
    public void setDescription(String description) { this.description = description; }
    public void setDays(String days) { this.days = days; }
    public void setMaxParticipants(int maxParticipants) { this.maxParticipants = maxParticipants; }
    public void setInstructorId(String instructorId) { this.instructorId = instructorId; }
    public void setInstructorName(String instructorName) { this.instructorName = instructorName; }
}
