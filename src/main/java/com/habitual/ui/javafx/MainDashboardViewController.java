package com.habitual.ui.javafx;

import com.habitual.habits.HabitService;
import com.habitual.habits.Habit;
import com.habitual.schedule.ScheduleService;
import com.habitual.schedule.ScheduledTask;
import com.habitual.utils.NotificationUtil;
import com.habitual.utils.MotivationalQuotes;
import com.habitual.utils.GeminiAIService;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.Node;

import java.io.IOException;

public class MainDashboardViewController {    @FXML
    private BorderPane mainPane;
    @FXML
    private Label welcomeLabel;
    @FXML
    private VBox contentArea;
    @FXML
    private Label motivationalQuoteLabel;

    private boolean darkMode = true; // Added dark mode flag
    private GeminiAIService aiService;    public void initialize() {
        aiService = new GeminiAIService();
        UserSession session = UserSession.getInstance();
        if (session.isLoggedIn()) {
            welcomeLabel.setText("Welcome, " + session.getUsername() + "!");
            
            // Load default motivational quote first
            loadMotivationalQuote();
            
            // Then try to get AI-generated personalized quote
            loadAIMotivationalMessage(session.getUserId());
            
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
            if (habitService.getCurrentStreak(habit) == 0) {
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
            // Show JavaFX alert
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Reminders");
            alert.setHeaderText("Don't forget!");
            alert.setContentText(msg.toString());
            alert.show();
            // Show system notification
            NotificationUtil.showNotification("Habitual Reminder", msg.toString());
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
        String darkThemePath = "/com/habitual/ui/javafx/futuristic-styles.css";
        String lightThemePath = "/com/habitual/ui/javafx/styles-light.css";
        
        // Clear existing stylesheets from the main pane and all loaded content
        mainPane.getStylesheets().clear();
        if (contentArea.getChildren().size() > 0) {
            Node currentContent = contentArea.getChildren().get(0);
            if (currentContent instanceof Parent) {
                ((Parent) currentContent).getStylesheets().clear();
            }
        }

        if (darkMode) {
            // Switch to light mode
            try {
                String lightThemeUrl = getClass().getResource(lightThemePath).toExternalForm();
                mainPane.getStylesheets().add(lightThemeUrl);
                if (contentArea.getChildren().size() > 0) {
                    Node currentContent = contentArea.getChildren().get(0);
                    if (currentContent instanceof Parent) {
                        ((Parent) currentContent).getStylesheets().add(lightThemeUrl);
                    }
                }
                darkMode = false;
            } catch (NullPointerException e) {
                System.err.println("Error: Could not find light theme stylesheet: " + lightThemePath);
                // Optionally, revert to dark mode or show an error to the user
                // For now, let's try to re-apply dark mode if light is missing
                try {
                    mainPane.getStylesheets().add(getClass().getResource(darkThemePath).toExternalForm());
                } catch (NullPointerException ex) {
                     System.err.println("Error: Could not find dark theme stylesheet either: " + darkThemePath);
                }
            }
        } else {
            // Switch to dark mode
            try {
                String darkThemeUrl = getClass().getResource(darkThemePath).toExternalForm();
                mainPane.getStylesheets().add(darkThemeUrl);
                 if (contentArea.getChildren().size() > 0) {
                    Node currentContent = contentArea.getChildren().get(0);
                    if (currentContent instanceof Parent) {
                        ((Parent) currentContent).getStylesheets().add(darkThemeUrl);
                    }
                }
                darkMode = true;
            } catch (NullPointerException e) {
                System.err.println("Error: Could not find dark theme stylesheet: " + darkThemePath);
                // Optionally, show an error to the user
            }
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

    private void loadView(String fxmlFile) {
        try {
            Node view = FXMLLoader.load(getClass().getResource(fxmlFile));
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
            // Handle error loading view, maybe show an error message in the UI
            Label errorLabel = new Label("Error loading view: " + fxmlFile);
            contentArea.getChildren().setAll(errorLabel);
        }
    }

    @FXML
    private void handleNotesButtonAction() {
        loadView("NotesView.fxml");
    }    private void loadMotivationalQuote() {
        String quote = MotivationalQuotes.getRandomQuote();
        if (motivationalQuoteLabel != null) {
            motivationalQuoteLabel.setText("\"" + quote + "\"");
        }
    }
    
    /**
     * Loads a personalized AI-generated motivational message based on user's habits and streak data
     * @param userId The user ID to get habit data for
     */
    private void loadAIMotivationalMessage(int userId) {
        try {
            Connection conn = com.habitual.db.DatabaseConnector.getConnection();
            HabitService habitService = new HabitService(conn);
            
            // Get user's habits and completion data
            List<Habit> habits = habitService.getHabits(userId);
            int completedToday = 0;
            StringBuilder streakInfo = new StringBuilder();
            
            for (Habit habit : habits) {
                int streak = habitService.getCurrentStreak(habit);
                if (streak > 0) {
                    completedToday++;
                    streakInfo.append(habit.getName()).append(": ").append(streak).append(" day streak. ");
                }
            }
            
            // Generate AI message asynchronously
            final int completedTodayFinal = completedToday;
            final String streakInfoFinal = streakInfo.toString();
            CompletableFuture.supplyAsync(() -> {
                try {
                    return aiService.generateMotivationalMessage(completedTodayFinal, streakInfoFinal);
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }).thenAccept(message -> {
                if (message != null && !message.isEmpty()) {
                    Platform.runLater(() -> {
                        if (motivationalQuoteLabel != null) {
                            motivationalQuoteLabel.setText("\"" + message + "\"");
                        }
                    });
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            // Fallback to regular quote
            loadMotivationalQuote();
        }
    }
}
