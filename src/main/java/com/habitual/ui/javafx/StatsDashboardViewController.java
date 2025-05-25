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
        for (Habit habit : habits) {
            int streak = habitService.getCurrentStreak(habit.getId());
            streakSeries.getData().add(new BarChart.Data<>(habit.getName(), streak));
            // For pie chart, you could use streak or completion ratio (here, just streak for demo)
            pieChartData.add(new PieChart.Data(habit.getName(), streak));
        }
        habitStreakBarChart.getData().add(streakSeries);
        habitCompletionPieChart.setData(pieChartData);
    }
}
