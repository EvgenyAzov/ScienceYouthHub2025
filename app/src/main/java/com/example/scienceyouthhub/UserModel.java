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

    public UserModel() {}

    // Full constructor
    public UserModel(String id, String name, int age, String type, String parentId, List<String> myActivities) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.type = type;
        this.parentId = parentId;
        this.myActivities = myActivities;
    }

    // Short constructor for compatibility (used for registration!)
    public UserModel(String id, String name, int age, String type) {
        this(id, name, age, type, null, null);
    }

    // Getters/setters ...
    public String getId() { return id; }
    public String getName() { return name; }
    public int getAge() { return age; }
    public String getType() { return type; }
    public String getParentId() { return parentId; }
    public List<String> getMyActivities() { return myActivities; }

    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setAge(int age) { this.age = age; }
    public void setType(String type) { this.type = type; }
    public void setParentId(String parentId) { this.parentId = parentId; }
    public void setMyActivities(List<String> myActivities) { this.myActivities = myActivities; }

    // Firestore utils
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("name", name);
        map.put("age", age);
        map.put("type", type);
        map.put("parentId", parentId);
        map.put("myActivities", myActivities);
        return map;
    }

    public static UserModel fromMap(Map<String, Object> map) {
        return new UserModel(
                (String) map.get("id"),
                (String) map.get("name"),
                ((Long) map.get("age")).intValue(),
                (String) map.get("type"),
                (String) map.get("parentId"),
                (List<String>) map.get("myActivities")
        );
    }
}
