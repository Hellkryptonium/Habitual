package com.habitual.habits;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Habit {
    private int id;
    private int userId;
    private String name;
    private String description;
    private String frequency; // e.g., "daily", "weekly"
    private LocalDate startDate;
    private LocalDateTime createdAt;
    private List<LocalDate> completionDates; // New field for tracking completions
    private int currentStreak; // New field
    private int longestStreak; // New field

    // Constructor
    public Habit(int id, int userId, String name, String description, String frequency, LocalDate startDate, LocalDateTime createdAt, List<LocalDate> completionDates, int currentStreak, int longestStreak) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.description = description;
        this.frequency = frequency;
        this.startDate = startDate;
        this.createdAt = createdAt;
        this.completionDates = completionDates != null ? new ArrayList<>(completionDates) : new ArrayList<>();
        this.currentStreak = currentStreak;
        this.longestStreak = longestStreak;
    }

    // Overloaded constructor for creating new habits
    public Habit(int userId, String name, String description, String frequency, LocalDate startDate) {
        this(0, userId, name, description, frequency, startDate, LocalDateTime.now(), new ArrayList<>(), 0, 0);
    }


    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<LocalDate> getCompletionDates() {
        return completionDates;
    }

    public void setCompletionDates(List<LocalDate> completionDates) {
        this.completionDates = completionDates != null ? new ArrayList<>(completionDates) : new ArrayList<>();
    }

    public void addCompletionDate(LocalDate date) {
        if (this.completionDates == null) {
            this.completionDates = new ArrayList<>();
        }
        if (date != null && !this.completionDates.contains(date)) {
            this.completionDates.add(date);
            // Potentially update streaks here or in a service layer
        }
    }

    public int getCurrentStreak() {
        return currentStreak;
    }

    public void setCurrentStreak(int currentStreak) {
        this.currentStreak = currentStreak;
    }

    public int getLongestStreak() {
        return longestStreak;
    }

    public void setLongestStreak(int longestStreak) {
        this.longestStreak = longestStreak;
    }

    // toString method
    @Override
    public String toString() {
        return "Habit{" +
                "id=" + id +
                ", userId=" + userId +
                ", name='" + name + "'" +
                ", description='" + description + "'" +
                ", frequency='" + frequency + "'" +
                ", startDate=" + startDate +
                ", createdAt=" + createdAt +
                ", completionDates=" + completionDates +
                ", currentStreak=" + currentStreak +
                ", longestStreak=" + longestStreak +
                '}';
    }
}
