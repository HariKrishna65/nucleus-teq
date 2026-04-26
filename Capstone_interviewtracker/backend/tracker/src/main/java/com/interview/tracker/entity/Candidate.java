package com.interview.tracker.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "candidates")
public class Candidate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String phone;
    private Double experience;
    private Double currentCtc;
    private Double expectedCtc;
    private Integer noticePeriod;
    private String status;

    // 🔗 Link to User (owner of this candidate)
    @JsonIgnore   // avoid infinite JSON loop
    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    // 🔗 Link to JobDescription
    @ManyToOne
    @JoinColumn(name = "jd_id")
    private JobDescription jd;

    public Candidate() {}

    // getters & setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public Double getExperience() { return experience; }
    public void setExperience(Double experience) { this.experience = experience; }

    public Double getCurrentCtc() { return currentCtc; }
    public void setCurrentCtc(Double currentCtc) { this.currentCtc = currentCtc; }

    public Double getExpectedCtc() { return expectedCtc; }
    public void setExpectedCtc(Double expectedCtc) { this.expectedCtc = expectedCtc; }

    public Integer getNoticePeriod() { return noticePeriod; }
    public void setNoticePeriod(Integer noticePeriod) { this.noticePeriod = noticePeriod; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public JobDescription getJd() { return jd; }
    public void setJd(JobDescription jd) { this.jd = jd; }
}