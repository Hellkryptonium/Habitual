package com.habitual.ui.javafx;

import com.habitual.habits.HabitService;
import com.habitual.habits.Habit;
import com.habitual.schedule.ScheduleService;
import com.habitual.schedule.ScheduledTask;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class MainDashboardViewController {

    @FXML
    private BorderPane mainPane;
    @FXML
    private Label welcomeLabel;
    @FXML
    private VBox contentArea;

    private boolean darkMode = true; // Added dark mode flag

    public void initialize() {
        UserSession session = UserSession.getInstance();
        if (session.isLoggedIn()) {
            welcomeLabel.setText("Welcome, " + session.getUsername() + "!");
            // Show reminders/notifications
            showReminders(session.getUserId());
        } else {
            // This should ideally not happen if navigation is correct
            welcomeLabel.setText("Welcome!");
            // Optionally, redirect to login if no active session
            try {
                MainFxApp.loadScene("LoginView.fxml", "Habitual - Login");
            } catch (IOException e) {
                e.printStackTrace(); // Log error
                // Display error to user or handle appropriately
            }
        }
        // Load a default view into contentArea if desired
        // loadContent("DefaultDashboardContent.fxml"); 
    }

    private void showReminders(int userId) {
        Connection conn = com.habitual.db.DatabaseConnector.getConnection();
        HabitService habitService = new HabitService(conn);
        ScheduleService scheduleService = new ScheduleService(conn);
        List<Habit> habits = habitService.getHabits(userId);
        List<ScheduledTask> tasks = scheduleService.getTasks(userId, false);
        LocalDate today = LocalDate.now();
        boolean hasHabitDue = false;
        for (Habit habit : habits) {
            // If not completed today (simple version: streak is 0 today)
            if (habitService.getCurrentStreak(habit.getId()) == 0) {
                hasHabitDue = true;
                break;
            }
        }
        boolean hasTaskDue = false;
        for (ScheduledTask task : tasks) {
            if (task.getDueDate() != null && !task.isCompleted() &&
                task.getDueDate().toLocalDateTime().toLocalDate().equals(today)) {
                hasTaskDue = true;
                break;
            }
        }
        if (hasHabitDue || hasTaskDue) {
            StringBuilder msg = new StringBuilder("You have pending:");
            if (hasHabitDue) msg.append("\n- Habits to complete today");
            if (hasTaskDue) msg.append("\n- Tasks due today");
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Reminders");
            alert.setHeaderText("Don't forget!");
            alert.setContentText(msg.toString());
            alert.show();
        }
    }

    @FXML
    protected void handleLogoutAction() {
        UserSession.getInstance().clearSession();
        try {
            MainFxApp.loadScene("LoginView.fxml", "Habitual - Login");
        } catch (IOException e) {
            e.printStackTrace();
            // Handle error, maybe show an alert
        }
    }

    @FXML
    protected void handleViewHabits() {
        loadContent("HabitsView.fxml");
        System.out.println("View Habits Clicked - loading HabitsView.fxml");
    }

    @FXML
    protected void handleAddHabit() {
        loadContent("AddHabitView.fxml");
        System.out.println("Add New Habit Clicked - loading AddHabitView.fxml");
    }

    @FXML
    protected void handleViewSchedule() {
        loadContent("ScheduleView.fxml");
        System.out.println("View Schedule Clicked - loading ScheduleView.fxml");
    }

    @FXML
    protected void handleAddTask() {
        loadContent("AddTaskView.fxml");
        System.out.println("Add New Task Clicked - loading AddTaskView.fxml");
    }

    @FXML
    protected void handleViewStats() {
        loadContent("StatsDashboardView.fxml");
        System.out.println("View Statistics Clicked - loading StatsDashboardView.fxml");
    }

    @FXML
    protected void handleChangeSecurityQuestion() {
        loadContent("ChangeSecurityQuestionView.fxml");
        System.out.println("Change Security Question Clicked - loading ChangeSecurityQuestionView.fxml");
    }

    @FXML
    protected void handleToggleTheme() { // Handler for dark/light mode toggle
        if (darkMode) {
            mainPane.getStylesheets().clear();
            mainPane.getStylesheets().add(getClass().getResource("/com/habitual/ui/javafx/styles-light.css").toExternalForm());
            darkMode = false;
        } else {
            mainPane.getStylesheets().clear();
            mainPane.getStylesheets().add(getClass().getResource("/com/habitual/ui/javafx/styles.css").toExternalForm());
            darkMode = true;
        }
    }

    // Helper method to load FXML content into the contentArea VBox
    private void loadContent(String fxmlFile) {
        try {
            // Ensure the path is correct, relative to the resources folder
            // If your FXML files are directly under src/main/resources/com/habitual/ui/javafx/
            // then the path should start with /com/habitual/ui/javafx/
            String fullPath = "/com/habitual/ui/javafx/" + fxmlFile;
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fullPath));
            if (loader.getLocation() == null) {
                throw new IOException("Cannot find FXML file: " + fullPath + ". Check the path.");
            }
            Parent view = loader.load();
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
            contentArea.getChildren().clear();
            Label errorLabel = new Label("Error loading content: " + fxmlFile);
            errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 14px;");
            contentArea.getChildren().add(errorLabel);
        }
    }
}
