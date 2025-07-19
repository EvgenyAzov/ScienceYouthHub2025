package com.example.scienceyouthhub;

public class UserModel {
    private String id;
    private String name;
    private int age;
    private String type;

    public UserModel(String id, String name, int age, String type) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.type = type;
    }

    // Геттеры и сеттеры
    public String getId() { return id; }
    public String getName() { return name; }
    public int getAge() { return age; }
    public String getType() { return type; }
}