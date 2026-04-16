package com.example.spring_rest_app.model;

public class User {

    private Long id;
    private String name;
    private Integer age;
    private String role;

    public User(Long id, String name, Integer age, String role) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.role = role;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public Integer getAge() { return age; }
    public String getRole() { return role; }
}
