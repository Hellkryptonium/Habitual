package com.habitual.schedule;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class ScheduleService {
    private Connection dbConnection;

    public ScheduleService(Connection connection) {
        this.dbConnection = connection;
    }

    public boolean addTask(int userId, String title, String description, Timestamp dueDate, int priority) {
        String sql = "INSERT INTO scheduled_tasks (user_id, title, description, due_date, priority) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = dbConnection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, title);
            pstmt.setString(3, description);
            pstmt.setTimestamp(4, dueDate);
            pstmt.setInt(5, priority);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<ScheduledTask> getTasks(int userId, boolean includeCompleted) {
        List<ScheduledTask> tasks = new ArrayList<>();
        String sql = "SELECT id, title, description, due_date, priority, is_completed, created_at FROM scheduled_tasks WHERE user_id = ?";
        if (!includeCompleted) {
            sql += " AND is_completed = FALSE";
        }
        sql += " ORDER BY due_date ASC, priority ASC";

        try (PreparedStatement pstmt = dbConnection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                ScheduledTask task = new ScheduledTask(
                        rs.getInt("id"),
                        userId,
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getTimestamp("due_date"),
                        rs.getInt("priority"),
                        rs.getBoolean("is_completed"),
                        rs.getTimestamp("created_at")
                );
                tasks.add(task);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tasks;
    }

    public boolean markTaskComplete(int taskId, boolean isCompleted) {
        String sql = "UPDATE scheduled_tasks SET is_completed = ? WHERE id = ?";
        try (PreparedStatement pstmt = dbConnection.prepareStatement(sql)) {
            pstmt.setBoolean(1, isCompleted);
            pstmt.setInt(2, taskId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // You can add other methods like updateTask, deleteTask, etc.
}