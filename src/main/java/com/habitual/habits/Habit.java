package com.habitual.habits;

import java.sql.Timestamp;

public class Habit {
    private int id;
    private int userId;
    private String name;
    private String description;
    private String frequency;
    private Timestamp createdAt;

    // Constructor
    public Habit(int id, int userId, String name, String description, String frequency, Timestamp createdAt) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.description = description;
        this.frequency = frequency;
        this.createdAt = createdAt;
    }

    // Getters
    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getFrequency() {
        return frequency;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    // Setters (optional, depending on whether habits are mutable after creation)
    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    @Override
    public String toString() {
        return "Habit{" +
                "id=" + id +
                ", userId=" + userId +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", frequency='" + frequency + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
