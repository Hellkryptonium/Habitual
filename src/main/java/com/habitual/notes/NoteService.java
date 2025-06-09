package com.habitual.notes;

import java.sql.Connection;
import java.util.List;

public class NoteService {
    private NoteDAO noteDAO;

    public NoteService(Connection connection) {
        this.noteDAO = new NoteDAO(connection);
    }

    public boolean addNote(int userId, String title, String content, List<String> tags) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Note title cannot be empty.");
        }
        // Content can be empty if user desires
        // Tags can be null or empty
        return noteDAO.addNote(userId, title, content, tags);
    }

    public List<Note> getNotes(int userId) {
        return noteDAO.getNotes(userId);
    }

    public boolean updateNote(int noteId, String title, String content, List<String> tags) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Note title cannot be empty.");
        }
        // Content can be empty
        // Tags can be null or empty, existing tags will be overwritten by the new list
        return noteDAO.updateNote(noteId, title, content, tags);
    }

    public boolean deleteNote(int noteId) {
        return noteDAO.deleteNote(noteId);
    }

    // Potentially add methods for tag-specific operations if needed in the future, e.g.:
    // public boolean addTagToNote(int noteId, String tag) { ... }
    // public boolean removeTagFromNote(int noteId, String tag) { ... }
    // public List<Note> findNotesByTag(int userId, String tag) { ... }
}
