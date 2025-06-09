package com.habitual.ui.javafx;

import com.habitual.db.DatabaseConnector;
import com.habitual.habits.Habit;
import com.habitual.habits.HabitService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;

import java.sql.Connection;
import java.util.List;

public class StatsDashboardViewController {
    @FXML
    private PieChart habitCompletionPieChart;
    @FXML
    private BarChart<String, Number> habitStreakBarChart;
    @FXML
    private CategoryAxis habitNameAxis;
    @FXML
    private NumberAxis streakAxis;
    @FXML
    private Label weeklyStatsLabel;

    private HabitService habitService;
    private UserSession userSession;

    public void initialize() {
        Connection conn = DatabaseConnector.getConnection();
        if (conn == null) {
            return;
        }
        habitService = new HabitService(conn);
        userSession = UserSession.getInstance();
        if (!userSession.isLoggedIn()) {
            return;
        }
        List<Habit> habits = habitService.getHabits(userSession.getUserId());
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        habitStreakBarChart.getData().clear();
        BarChart.Series<String, Number> streakSeries = new BarChart.Series<>();
        streakSeries.setName("Current Streak");
        int totalCompletionsThisWeek = 0;
        int totalHabits = habits.size();
        for (Habit habit : habits) {
            int streak = habitService.getCurrentStreak(habit);
            streakSeries.getData().add(new BarChart.Data<>(habit.getName(), streak));
            // For pie chart, you could use streak or completion ratio (here, just streak for demo)
            pieChartData.add(new PieChart.Data(habit.getName(), streak));
            // Calculate completions in the last 7 days
            totalCompletionsThisWeek += habitService.getCompletionsInLastNDays(habit.getId(), 7);
        }
        habitStreakBarChart.getData().add(streakSeries);
        habitCompletionPieChart.setData(pieChartData);
        // Show weekly stats
        if (weeklyStatsLabel != null) {
            weeklyStatsLabel.setText("Habits completed in last 7 days: " + totalCompletionsThisWeek + " / " + (totalHabits * 7));
        }
    }
}
