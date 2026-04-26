package com.interview.tracker.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "panel")
public class Panel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String organization;
    private String designation;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    public Panel() {}

    // getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getOrganization() { return organization; }
    public void setOrganization(String organization) { this.organization = organization; }

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}