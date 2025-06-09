package com.habitual.dao;

import com.habitual.habits.Habit;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class HabitDAO {
    private final Connection dbConnection;

    public HabitDAO(Connection connection) {
        this.dbConnection = connection;
    }

    public boolean addHabit(int userId, String name, String description, String frequency, LocalDate startDate) {
        // Assumes new columns: start_date, current_streak, longest_streak in habits table
        String sql = "INSERT INTO habits (user_id, name, description, frequency, start_date, created_at, current_streak, longest_streak) VALUES (?, ?, ?, ?, ?, NOW(), 0, 0)";
        try (PreparedStatement pstmt = dbConnection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, name);
            pstmt.setString(3, description);
            pstmt.setString(4, frequency);
            pstmt.setDate(5, java.sql.Date.valueOf(startDate));
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Habit> getHabits(int userId) {
        List<Habit> habits = new ArrayList<>();
        // Assumes columns: start_date, current_streak, longest_streak exist
        String sql = "SELECT id, name, description, frequency, start_date, created_at, current_streak, longest_streak FROM habits WHERE user_id = ? ORDER BY created_at DESC";
        try (PreparedStatement pstmt = dbConnection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                int habitId = rs.getInt("id");
                LocalDate startDate = rs.getDate("start_date") != null ? rs.getDate("start_date").toLocalDate() : null;
                LocalDateTime createdAt = rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null;
                List<LocalDate> completionDates = getCompletionDatesForHabit(habitId);
                
                Habit habit = new Habit(
                    habitId,
                    userId,
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getString("frequency"),
                    startDate,
                    createdAt,
                    completionDates,
                    rs.getInt("current_streak"),
                    rs.getInt("longest_streak")
                );
                habits.add(habit);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return habits;
    }

    private List<LocalDate> getCompletionDatesForHabit(int habitId) {
        List<LocalDate> dates = new ArrayList<>();
        String sql = "SELECT completion_date FROM habit_completions WHERE habit_id = ? ORDER BY completion_date ASC";
        try (PreparedStatement pstmt = dbConnection.prepareStatement(sql)) {
            pstmt.setInt(1, habitId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                dates.add(rs.getDate("completion_date").toLocalDate());
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Consider logging this error more formally
        }
        return dates;
    }

    public boolean updateHabitDetails(Habit habit) {
        String sql = "UPDATE habits SET name = ?, description = ?, frequency = ?, start_date = ? WHERE id = ?";
        try (PreparedStatement pstmt = dbConnection.prepareStatement(sql)) {
            pstmt.setString(1, habit.getName());
            pstmt.setString(2, habit.getDescription());
            pstmt.setString(3, habit.getFrequency());
            pstmt.setDate(4, habit.getStartDate() != null ? java.sql.Date.valueOf(habit.getStartDate()) : null);
            pstmt.setInt(5, habit.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateHabitStreaks(int habitId, int currentStreak, int longestStreak) {
        String sql = "UPDATE habits SET current_streak = ?, longest_streak = ? WHERE id = ?";
        try (PreparedStatement pstmt = dbConnection.prepareStatement(sql)) {
            pstmt.setInt(1, currentStreak);
            pstmt.setInt(2, longestStreak);
            pstmt.setInt(3, habitId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean logHabitCompletion(int habitId, LocalDate completionDate) {
        String sql = "INSERT INTO habit_completions (habit_id, completion_date) VALUES (?, ?)";
        try (PreparedStatement pstmt = dbConnection.prepareStatement(sql)) {
            pstmt.setInt(1, habitId);
            pstmt.setDate(2, java.sql.Date.valueOf(completionDate));
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            // Consider checking for duplicate entry if (habit_id, completion_date) is unique
            e.printStackTrace();
            return false;
        }
    }

    public boolean undoHabitCompletion(int habitId, LocalDate completionDate) {
        String sql = "DELETE FROM habit_completions WHERE habit_id = ? AND completion_date = ?";
        try (PreparedStatement pstmt = dbConnection.prepareStatement(sql)) {
            pstmt.setInt(1, habitId);
            pstmt.setDate(2, java.sql.Date.valueOf(completionDate));
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // This method calculates streak on-the-fly. Service might use this to verify or before updating stored streaks.
    public int calculateCurrentStreak(int habitId) {
        String sql = "SELECT completion_date FROM habit_completions WHERE habit_id = ? ORDER BY completion_date DESC";
        try (PreparedStatement pstmt = dbConnection.prepareStatement(sql)) {
            pstmt.setInt(1, habitId);
            ResultSet rs = pstmt.executeQuery();
            LocalDate today = LocalDate.now();
            int streak = 0;
            List<LocalDate> dates = new ArrayList<>();
            while(rs.next()) {
                dates.add(rs.getDate("completion_date").toLocalDate());
            }

            // Check if today is a completion date
            boolean todayCompleted = dates.contains(today);
            // Check if yesterday is a completion date for continuous streak
            boolean yesterdayCompleted = dates.contains(today.minusDays(1));

            if (todayCompleted) {
                streak = 1;
                LocalDate dayToCheck = today.minusDays(1);
                while(dates.contains(dayToCheck)){
                    streak++;
                    dayToCheck = dayToCheck.minusDays(1);
                }
            } else if (yesterdayCompleted) {
                // If not completed today, but yesterday, streak is based on yesterday
                 streak = 1; // for yesterday
                 LocalDate dayToCheck = today.minusDays(2);
                 while(dates.contains(dayToCheck)){
                    streak++;
                    dayToCheck = dayToCheck.minusDays(1);
                }
            } else {
                // Not completed today or yesterday
                streak = 0;
            }
            return streak;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public int getCompletionsInLastNDays(int habitId, int days) {
        String sql = "SELECT COUNT(*) FROM habit_completions WHERE habit_id = ? AND completion_date >= ?";
        try (PreparedStatement pstmt = dbConnection.prepareStatement(sql)) {
            pstmt.setInt(1, habitId);
            LocalDate since = LocalDate.now().minusDays(days - 1);
            pstmt.setDate(2, java.sql.Date.valueOf(since));
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public boolean deleteHabit(int habitId) {
        // Also consider deleting from habit_completions or use CASCADE DELETE in DB
        String deleteCompletionsSql = "DELETE FROM habit_completions WHERE habit_id = ?";
        try (PreparedStatement pstmt = dbConnection.prepareStatement(deleteCompletionsSql)) {
            pstmt.setInt(1, habitId);
            pstmt.executeUpdate(); // Execute deletion from child table first
        } catch (SQLException e) {
            e.printStackTrace();
            // Optionally, decide if failure here should prevent deleting the habit itself
        }

        String sql = "DELETE FROM habits WHERE id = ?";
        try (PreparedStatement pstmt = dbConnection.prepareStatement(sql)) {
            pstmt.setInt(1, habitId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
