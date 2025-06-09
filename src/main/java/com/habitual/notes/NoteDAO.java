package com.habitual.notes;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class NoteDAO {
    private final Connection dbConnection;

    public NoteDAO(Connection connection) {
        this.dbConnection = connection;
    }

    // Assuming 'tags' is a TEXT column storing comma-separated values
    // Assuming 'updated_at' in DB corresponds to 'lastModified' in Note.java
    public boolean addNote(int userId, String title, String content, List<String> tags) {
        String sql = "INSERT INTO notes (user_id, title, content, created_at, updated_at, tags) VALUES (?, ?, ?, NOW(), NOW(), ?)";
        try (PreparedStatement pstmt = dbConnection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, title);
            pstmt.setString(3, content);
            pstmt.setString(4, tags != null ? String.join(",", tags) : "");
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Note> getNotes(int userId) {
        List<Note> notes = new ArrayList<>();
        // Assuming 'tags' column exists and stores comma-separated tag strings
        String sql = "SELECT id, title, content, created_at, updated_at, tags FROM notes WHERE user_id = ? ORDER BY updated_at DESC";
        try (PreparedStatement pstmt = dbConnection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Timestamp createdAtTimestamp = rs.getTimestamp("created_at");
                Timestamp lastModifiedTimestamp = rs.getTimestamp("updated_at"); // Using updated_at for lastModified
                
                LocalDateTime createdAt = createdAtTimestamp != null ? createdAtTimestamp.toLocalDateTime() : null;
                LocalDateTime lastModified = lastModifiedTimestamp != null ? lastModifiedTimestamp.toLocalDateTime() : null;

                String tagsString = rs.getString("tags");
                List<String> tagsList = new ArrayList<>();
                if (tagsString != null && !tagsString.isEmpty()) {
                    tagsList.addAll(Arrays.asList(tagsString.split(",")));
                }
                
                Note note = new Note(
                    rs.getInt("id"),
                    userId,
                    rs.getString("title"),
                    rs.getString("content"),
                    createdAt,
                    lastModified,
                    tagsList
                );
                notes.add(note);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return notes;
    }

    public boolean updateNote(int noteId, String title, String content, List<String> tags) {
        String sql = "UPDATE notes SET title = ?, content = ?, updated_at = NOW(), tags = ? WHERE id = ?";
        try (PreparedStatement pstmt = dbConnection.prepareStatement(sql)) {
            pstmt.setString(1, title);
            pstmt.setString(2, content);
            pstmt.setString(3, tags != null ? String.join(",", tags) : "");
            pstmt.setInt(4, noteId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // ...existing code...
    // deleteNote method remains the same if tags are part of the notes table
    // and cascade delete is handled by DB or not strictly needed for tags alone.
    public boolean deleteNote(int noteId) {
        String sql = "DELETE FROM notes WHERE id = ?";
        try (PreparedStatement pstmt = dbConnection.prepareStatement(sql)) {
            pstmt.setInt(1, noteId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
