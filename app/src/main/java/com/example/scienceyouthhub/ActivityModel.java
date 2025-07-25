package com.example.scienceyouthhub;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class ActivityModel implements Serializable {
    private String id;
    private String name;
    private String category;    // наука, обществознание, творчество
    private String subcategory; // биология, робототехника и т.п.
    private String ageRange;
    private String description;
    private List<String> daysOfWeek;  // например ["Пн", "Ср", "Пт"]
    private String month;              // "January", "February" и т.п.
    private int maxParticipants;
    private String instructorId;   // один ID инструктора
    private String instructorName; // имя инструктора
    private Date startDate;  // дата начала
    private Date endDate;    // дата окончания
    private boolean approvedByAdmin;

    private String status; // “Плохо”, “Удовлетворительно”, “Отлично”
    private List<String> participants;

    // Пустой конструктор (важен для Firestore и сериализации)
    public ActivityModel() {
        this.approvedByAdmin = true; // по умолчанию true
    }

    public ActivityModel(String id, String name, String category, String subcategory, String ageRange,
                         String description, List<String> daysOfWeek, Date startDate, Date endDate,
                         int maxParticipants, String instructorId, String instructorName, boolean approvedByAdmin) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.subcategory = subcategory;
        this.ageRange = ageRange;
        this.description = description;
        this.daysOfWeek = daysOfWeek;
        this.startDate = startDate;
        this.endDate = endDate;
        this.maxParticipants = maxParticipants;
        this.instructorId = instructorId;
        this.instructorName = instructorName;
        this.approvedByAdmin = approvedByAdmin;
    }

    // Фабричный метод для создания новой активности с уникальным id
    public static ActivityModel createNew(String name, String category, String subcategory, String ageRange,
                                          String description, List<String> daysOfWeek, Date startDate, Date endDate,
                                          int maxParticipants, String instructorId, String instructorName,
                                          boolean approvedByAdmin) {
        return new ActivityModel(UUID.randomUUID().toString(), name, category, subcategory, ageRange,
                description, daysOfWeek, startDate, endDate, maxParticipants, instructorId, instructorName, approvedByAdmin);
    }

    // Геттеры и сеттеры для всех полей
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getSubcategory() { return subcategory; }
    public void setSubcategory(String subcategory) { this.subcategory = subcategory; }

    public String getAgeRange() { return ageRange; }
    public void setAgeRange(String ageRange) { this.ageRange = ageRange; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<String> getDaysOfWeek() { return daysOfWeek; }
    public void setDaysOfWeek(List<String> daysOfWeek) { this.daysOfWeek = daysOfWeek; }

    public String getMonth() { return month; }
    public void setMonth(String month) { this.month = month; }

    public int getMaxParticipants() { return maxParticipants; }
    public void setMaxParticipants(int maxParticipants) { this.maxParticipants = maxParticipants; }

    public String getInstructorId() { return instructorId; }
    public void setInstructorId(String instructorId) { this.instructorId = instructorId; }

    public String getInstructorName() { return instructorName; }
    public void setInstructorName(String instructorName) { this.instructorName = instructorName; }

    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }

    public Date getEndDate() { return endDate; }
    public void setEndDate(Date endDate) { this.endDate = endDate; }

    public boolean isApprovedByAdmin() { return approvedByAdmin; }
    public void setApprovedByAdmin(boolean approvedByAdmin) { this.approvedByAdmin = approvedByAdmin; }

    public List<String> getParticipants() { return participants; }
    public void setParticipants(List<String> participants) { this.participants = participants; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
