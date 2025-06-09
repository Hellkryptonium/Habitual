package com.habitual.habits;

import com.habitual.dao.HabitDAO;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class HabitService {
    private HabitDAO habitDAO;

    public HabitService(Connection connection) {
        this.habitDAO = new HabitDAO(connection);
    }

    public boolean addHabit(int userId, String name, String description, String frequency, LocalDate startDate) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Habit name cannot be empty.");
        }
        if (startDate == null) {
            throw new IllegalArgumentException("Start date cannot be null.");
        }
        // Frequency validation could be added here if there's a predefined set
        return habitDAO.addHabit(userId, name, description, frequency, startDate);
    }

    public List<Habit> getHabits(int userId) {
        // The DAO's getHabits method is now responsible for populating all fields, including streaks and completion dates.
        return habitDAO.getHabits(userId);
    }

    public boolean logHabitCompletion(int habitId, LocalDate completionDate) {
        if (completionDate == null) {
            throw new IllegalArgumentException("Completion date cannot be null.");
        }
        boolean logged = habitDAO.logHabitCompletion(habitId, completionDate);
        if (logged) {
            return updateStreaksForHabit(habitId);
        }
        return false;
    }

    public boolean undoHabitCompletion(int habitId, LocalDate completionDate) {
        if (completionDate == null) {
            throw new IllegalArgumentException("Completion date cannot be null.");
        }
        boolean undone = habitDAO.undoHabitCompletion(habitId, completionDate);
        if (undone) {
            return updateStreaksForHabit(habitId);
        }
        return false;
    }

    private boolean updateStreaksForHabit(int habitId) {
        Habit habit = getHabitById(habitId); // Need a method to get a single habit
        if (habit == null) {
            // Or throw an exception
            System.err.println("Habit not found for streak update: " + habitId);
            return false;
        }

        List<LocalDate> completionDates = habit.getCompletionDates(); // Assuming DAO populates this correctly in getHabitById
        // Sort dates to be sure, though DAO should return them sorted
        Collections.sort(completionDates);

        int newCurrentStreak = calculateCurrentStreakInternal(completionDates, habit.getFrequency());
        int newLongestStreak = Math.max(habit.getLongestStreak(), newCurrentStreak);

        habit.setCurrentStreak(newCurrentStreak);
        habit.setLongestStreak(newLongestStreak);
        
        return habitDAO.updateHabitStreaks(habitId, newCurrentStreak, newLongestStreak);
    }

    // Internal method to calculate current streak based on sorted completion dates
    // This is a simplified daily streak calculation. More complex logic needed for other frequencies.
    private int calculateCurrentStreakInternal(List<LocalDate> completionDates, String frequency) {
        if (completionDates == null || completionDates.isEmpty()) {
            return 0;
        }
        // Assuming daily frequency for now
        if (!"daily".equalsIgnoreCase(frequency)) {
            // For non-daily habits, streak calculation is more complex and needs specific rules.
            // Placeholder: return 0 or count of completions for simplicity until rules are defined.
            // System.err.println("Streak calculation for frequency '" + frequency + "' is not yet fully implemented.");
            // return completionDates.size(); // Or some other logic
            // For now, let's stick to a simple daily logic and assume it applies or needs refinement.
        }

        int streak = 0;
        LocalDate today = LocalDate.now();
        List<LocalDate> sortedDates = completionDates.stream().sorted(Collections.reverseOrder()).collect(Collectors.toList());

        LocalDate lastCompletionDate = sortedDates.get(0);

        // Check if the habit was completed today or yesterday to continue a streak
        if (!lastCompletionDate.isEqual(today) && !lastCompletionDate.isEqual(today.minusDays(1))) {
            // If the last completion was not today or yesterday, the streak is broken (unless it's today's first completion)
            if (lastCompletionDate.isBefore(today.minusDays(1))) return 0;
        }
        
        // If completed today, start from today. If completed yesterday, start from yesterday.
        LocalDate currentDateToCheck = lastCompletionDate.isEqual(today) ? today : today.minusDays(1);
        
        if (!sortedDates.contains(currentDateToCheck) && sortedDates.contains(today)){
             currentDateToCheck = today; // If not completed on expected check day, but was completed today.
        }

        for (LocalDate date : sortedDates) {
            if (date.isEqual(currentDateToCheck)) {
                streak++;
                currentDateToCheck = currentDateToCheck.minusDays(1);
            } else if (date.isBefore(currentDateToCheck)) {
                // Missed a day
                break;
            }
            // If date is after currentDateToCheck, it means there are multiple completions on the same day or unsorted list - ignore for streak.
        }
        return streak;
    }

    // Helper to get a single habit, needed for updateStreaksForHabit
    // This might involve fetching from DB or a cache if available
    public Habit getHabitById(int habitId) {
        // This is a simplified way; ideally, DAO would have a getHabitById method.
        // For now, filtering from getHabits(userId) is inefficient if userId is unknown or for general use.
        // Let's assume a more direct fetch or add one to DAO if necessary.
        // Placeholder: 
        List<Habit> allHabitsForUser = habitDAO.getHabits(findUserIdForHabit(habitId)); // This is problematic, need a direct DAO method
        for (Habit habit : allHabitsForUser) {
            if (habit.getId() == habitId) {
                // Crucially, ensure completionDates are populated by getHabits or a specific getHabitById DAO method
                return habit;
            }
        }
        return null; // Or throw HabitNotFoundException
    }

    // This is a placeholder for a method that would find the user ID for a given habit ID.
    // In a real application, this might not be needed if methods always have userId context,
    // or the Habit object itself contains userId.
    private int findUserIdForHabit(int habitId) {
        // This would require a DB query. For now, returning a placeholder.
        // This highlights a design consideration: service methods might need userId, or DAO needs to be more flexible.
        // For the purpose of this example, we'll assume the Habit object from getHabits already has completion dates.
        // And that updateStreaksForHabit will be called in a context where the habit object (with dates) is already available.
        // So, getHabitById might be better if it directly calls a DAO method like habitDAO.getHabitById(habitId)
        // which should also populate completion dates.
        System.err.println("findUserIdForHabit is a placeholder and needs proper implementation or DAO enhancement.");
        return -1; // Invalid user ID
    }

    public boolean updateHabit(Habit habit) {
        if (habit == null) {
            throw new IllegalArgumentException("Habit cannot be null.");
        }
        if (habit.getName() == null || habit.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Habit name cannot be empty.");
        }
        // Note: This only updates details, not streaks or completions directly.
        // Streaks are managed by log/undo completion.
        return habitDAO.updateHabitDetails(habit);
    }

    // getCurrentStreak now directly returns the stored value from the Habit object.
    // The DAO's calculateCurrentStreak can be used internally if needed for recalculations.
    // This method assumes you have a Habit object instance.
    public int getCurrentStreak(Habit habit) {
        if (habit == null) return 0;
        return habit.getCurrentStreak();
    }

    public int getLongestStreak(Habit habit) {
        if (habit == null) return 0;
        return habit.getLongestStreak();
    }

    public int getCompletionsInLastNDays(int habitId, int days) {
        return habitDAO.getCompletionsInLastNDays(habitId, days);
    }

    public boolean deleteHabit(int habitId) {
        // DAO's deleteHabit should handle deletion of completions as well (cascade or explicit)
        return habitDAO.deleteHabit(habitId);
    }
}