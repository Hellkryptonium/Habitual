package com.habitual.notes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList; // Added for initializing tags

public class Note {
    private int id;
    private int userId;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime lastModified; // New field
    private List<String> tags; // New field

    // Constructor
    public Note(int id, int userId, String title, String content, LocalDateTime createdAt, LocalDateTime lastModified, List<String> tags) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
        this.lastModified = lastModified;
        this.tags = tags != null ? new ArrayList<>(tags) : new ArrayList<>();
    }

    // Overloaded constructor for creating new notes without an ID yet
    public Note(int userId, String title, String content) {
        this(0, userId, title, content, LocalDateTime.now(), LocalDateTime.now(), new ArrayList<>());
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
        this.lastModified = LocalDateTime.now(); // Update lastModified when content changes
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastModified() {
        return lastModified;
    }

    public void setLastModified(LocalDateTime lastModified) {
        this.lastModified = lastModified;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags != null ? new ArrayList<>(tags) : new ArrayList<>();
        this.lastModified = LocalDateTime.now(); // Update lastModified when tags change
    }

    public void addTag(String tag) {
        if (this.tags == null) {
            this.tags = new ArrayList<>();
        }
        if (tag != null && !tag.trim().isEmpty() && !this.tags.contains(tag.trim())) {
            this.tags.add(tag.trim());
            this.lastModified = LocalDateTime.now(); // Update lastModified
        }
    }

    public void removeTag(String tag) {
        if (this.tags != null && tag != null) {
            this.tags.remove(tag.trim());
            this.lastModified = LocalDateTime.now(); // Update lastModified
        }
    }

    @Override
    public String toString() {
        return "Note{" +
                "id=" + id +
                ", userId=" + userId +
                ", title='" + title + "'" +
                ", content='" + content + "'" +
                ", createdAt=" + createdAt +
                ", lastModified=" + lastModified +
                ", tags=" + tags +
                '}';
    }
}
