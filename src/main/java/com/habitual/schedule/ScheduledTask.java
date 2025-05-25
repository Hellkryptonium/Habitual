package com.habitual.schedule;

import java.sql.Timestamp;

public class ScheduledTask {
    private int id;
    private int userId;
    private String title;
    private String description;
    private Timestamp dueDate;
    private int priority;
    private boolean isCompleted;
    private Timestamp createdAt;

    // Constructor
    public ScheduledTask(int id, int userId, String title, String description, Timestamp dueDate, int priority, boolean isCompleted, Timestamp createdAt) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.priority = priority;
        this.isCompleted = isCompleted;
        this.createdAt = createdAt;
    }

    // Getters
    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Timestamp getDueDate() {
        return dueDate;
    }

    public int getPriority() {
        return priority;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    // Setters (optional)
    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDueDate(Timestamp dueDate) {
        this.dueDate = dueDate;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    @Override
    public String toString() {
        return "ScheduledTask{" +
                "id=" + id +
                ", userId=" + userId +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", dueDate=" + dueDate +
                ", priority=" + priority +
                ", isCompleted=" + isCompleted +
                ", createdAt=" + createdAt +
                '}';
    }
}
