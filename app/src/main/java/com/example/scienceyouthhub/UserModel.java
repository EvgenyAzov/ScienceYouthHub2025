package com.example.scienceyouthhub;

import java.util.HashMap;
import java.util.Map;

public class UserModel {
    private String id;
    private String name;
    private int age;
    private String type; // User role

    public UserModel() {}

    public UserModel(String id, String name, int age, String type) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.type = type;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public int getAge() { return age; }
    public String getType() { return type; }

    // Setters (если нужны)
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setAge(int age) { this.age = age; }
    public void setType(String type) { this.type = type; }

    // Для Firestore
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("name", name);
        map.put("age", age);
        map.put("type", type);
        return map;
    }

    public static UserModel fromMap(Map<String, Object> map) {
        return new UserModel(
                (String) map.get("id"),
                (String) map.get("name"),
                ((Long) map.get("age")).intValue(), // Firestore возвращает Long
                (String) map.get("type")
        );
    }
}
