package com.habitual.habits;

import com.habitual.dao.HabitDAO;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class HabitService {
    private Connection dbConnection;
    private HabitDAO habitDAO;

    public HabitService(Connection connection) {
        this.dbConnection = connection;
        this.habitDAO = new HabitDAO(connection);
    }

    public boolean addHabit(int userId, String name, String description, String frequency) {
        return habitDAO.addHabit(userId, name, description, frequency);
    }

    public List<Habit> getHabits(int userId) {
        return habitDAO.getHabits(userId);
    }

    public boolean logHabitCompletion(int habitId, String completionDate) {
        return habitDAO.logHabitCompletion(habitId, completionDate);
    }

    public int getCurrentStreak(int habitId) {
        return habitDAO.getCurrentStreak(habitId);
    }
    
    // You can add other methods like updateHabit, deleteHabit, getHabitCompletions, etc.
}