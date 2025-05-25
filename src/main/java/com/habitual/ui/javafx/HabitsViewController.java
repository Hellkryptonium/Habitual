package com.habitual.ui.javafx;

import com.habitual.db.DatabaseConnector;
import com.habitual.habits.Habit;
import com.habitual.habits.HabitService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;

public class HabitsViewController {

    @FXML
    private ListView<Habit> habitsListView;
    @FXML
    private Button markCompleteButton;
    @FXML
    private Button refreshButton;
    @FXML
    private Label statusLabel;

    private HabitService habitService;
    private UserSession userSession;
    private final ObservableList<Habit> observableHabitsList = FXCollections.observableArrayList();

    public void initialize() {
        Connection conn = DatabaseConnector.getConnection();
        if (conn == null) {
            statusLabel.setText("Error: Database connection failed.");
            statusLabel.setStyle("-fx-text-fill: red;");
            markCompleteButton.setDisable(true);
            refreshButton.setDisable(true);
            return;
        }
        habitService = new HabitService(conn);
        userSession = UserSession.getInstance();

        // Custom cell factory to display habit name, description, and streak
        habitsListView.setCellFactory(param -> new ListCell<Habit>() {
            @Override
            protected void updateItem(Habit habit, boolean empty) {
                super.updateItem(habit, empty);
                if (empty || habit == null || habit.getName() == null) {
                    setText(null);
                } else {
                    int streak = 0;
                    if (habitService != null) {
                        streak = habitService.getCurrentStreak(habit.getId());
                    }
                    String desc = (habit.getDescription() != null && !habit.getDescription().isEmpty()) ? "\n  └ " + habit.getDescription() : "";
                    setText(habit.getName() + desc + "\n  └ Streak: " + streak + " day" + (streak == 1 ? "" : "s"));
                }
            }
        });

        habitsListView.setItems(observableHabitsList);
        loadHabits();
    }

    @FXML
    protected void loadHabits() {
        if (!userSession.isLoggedIn()) {
            statusLabel.setText("User not logged in.");
            statusLabel.setStyle("-fx-text-fill: red;");
            observableHabitsList.clear();
            return;
        }
        try {
            List<Habit> userHabits = habitService.getHabits(userSession.getUserId());
            observableHabitsList.setAll(userHabits);
            if (userHabits.isEmpty()) {
                statusLabel.setText("No habits found. Add some!");
                statusLabel.setStyle("-fx-text-fill: orange;");
            } else {
                statusLabel.setText("Habits loaded.");
                statusLabel.setStyle("-fx-text-fill: green;");
            }
        } catch (Exception e) {
            statusLabel.setText("Error loading habits: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: red;");
            e.printStackTrace();
        }
    }

    @FXML
    protected void handleMarkHabitCompleteAction() {
        Habit selectedHabit = habitsListView.getSelectionModel().getSelectedItem();
        if (selectedHabit == null) {
            statusLabel.setText("Please select a habit to mark as complete.");
            statusLabel.setStyle("-fx-text-fill: orange;");
            return;
        }

        if (!userSession.isLoggedIn()) {
            statusLabel.setText("User not logged in.");
            return;
        }

        // The service expects habitId and a String date 'YYYY-MM-DD'
        String completionDateStr = LocalDate.now().toString(); 
        boolean success = habitService.logHabitCompletion(selectedHabit.getId(), completionDateStr);

        if (success) {
            statusLabel.setText("Habit '" + selectedHabit.getName() + "' marked as complete for today!");
            statusLabel.setStyle("-fx-text-fill: green;");
            // Optionally, refresh the list or update UI to show completion status
        } else {
            statusLabel.setText("Failed to mark habit complete. Already logged for today or database error.");
            statusLabel.setStyle("-fx-text-fill: red;");
        }
    }
}
