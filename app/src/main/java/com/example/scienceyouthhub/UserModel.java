package com.example.scienceyouthhub;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserModel {
    private String id;
    private String name;
    private int age;
    private String type;
    private String parentId;
    private List<String> myActivities;
    // --- Добавлено для Instructor ---
    private String category;
    private String subcategory;

    public UserModel() {}

    // Полный конструктор (добавлены category и subcategory в конец)
    public UserModel(String id, String name, int age, String type, String parentId, List<String> myActivities,
                     String category, String subcategory) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.type = type;
        this.parentId = parentId;
        this.myActivities = myActivities;
        this.category = category;
        this.subcategory = subcategory;
    }

    // Короткий конструктор (для других ролей/старой логики)
    public UserModel(String id, String name, int age, String type) {
        this(id, name, age, type, null, null, null, null);
    }

    // Геттеры/сеттеры
    public String getId() { return id; }
    public String getName() { return name; }
    public int getAge() { return age; }
    public String getType() { return type; }
    public String getParentId() { return parentId; }
    public List<String> getMyActivities() { return myActivities; }
    public String getCategory() { return category; }
    public String getSubcategory() { return subcategory; }

    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setAge(int age) { this.age = age; }
    public void setType(String type) { this.type = type; }
    public void setParentId(String parentId) { this.parentId = parentId; }
    public void setMyActivities(List<String> myActivities) { this.myActivities = myActivities; }
    public void setCategory(String category) { this.category = category; }
    public void setSubcategory(String subcategory) { this.subcategory = subcategory; }

    // Firestore utils
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("name", name);
        map.put("age", age);
        map.put("type", type);
        map.put("parentId", parentId);
        map.put("myActivities", myActivities);
        map.put("category", category);
        map.put("subcategory", subcategory);
        return map;
    }

    public static UserModel fromMap(Map<String, Object> map) {
        return new UserModel(
                (String) map.get("id"),
                (String) map.get("name"),
                map.get("age") instanceof Long ? ((Long) map.get("age")).intValue() : (Integer) map.get("age"),
                (String) map.get("type"),
                (String) map.get("parentId"),
                (List<String>) map.get("myActivities"),
                (String) map.get("category"),
                (String) map.get("subcategory")
        );
    }
}
