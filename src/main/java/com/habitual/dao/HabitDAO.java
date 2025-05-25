package com.habitual.dao;

import com.habitual.habits.Habit;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HabitDAO {
    private final Connection dbConnection;

    public HabitDAO(Connection connection) {
        this.dbConnection = connection;
    }

    public boolean addHabit(int userId, String name, String description, String frequency) {
        String sql = "INSERT INTO habits (user_id, name, description, frequency) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = dbConnection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, name);
            pstmt.setString(3, description);
            pstmt.setString(4, frequency);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Habit> getHabits(int userId) {
        List<Habit> habits = new ArrayList<>();
        String sql = "SELECT id, name, description, frequency, created_at FROM habits WHERE user_id = ?";
        try (PreparedStatement pstmt = dbConnection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Habit habit = new Habit(
                        rs.getInt("id"),
                        userId,
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getString("frequency"),
                        rs.getTimestamp("created_at")
                );
                habits.add(habit);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return habits;
    }

    public boolean logHabitCompletion(int habitId, String completionDate) {
        String sql = "INSERT INTO habit_completions (habit_id, completion_date) VALUES (?, ?)";
        try (PreparedStatement pstmt = dbConnection.prepareStatement(sql)) {
            pstmt.setInt(1, habitId);
            pstmt.setDate(2, java.sql.Date.valueOf(completionDate));
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public int getCurrentStreak(int habitId) {
        String sql = "SELECT completion_date FROM habit_completions WHERE habit_id = ? ORDER BY completion_date DESC";
        try (PreparedStatement pstmt = dbConnection.prepareStatement(sql)) {
            pstmt.setInt(1, habitId);
            ResultSet rs = pstmt.executeQuery();
            java.time.LocalDate today = java.time.LocalDate.now();
            int streak = 0;
            while (rs.next()) {
                java.time.LocalDate date = rs.getDate("completion_date").toLocalDate();
                if (date.equals(today.minusDays(streak))) {
                    streak++;
                } else {
                    break;
                }
            }
            return streak;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }
}
