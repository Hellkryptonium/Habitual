package com.habitual.ui.javafx;

import com.habitual.db.DatabaseConnector;
import com.habitual.habits.Habit;
import com.habitual.habits.HabitService;
import com.habitual.utils.GeminiAIService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.sql.Connection;
import java.util.concurrent.CompletableFuture;
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
    private Label statusLabel;    @FXML
    private Button undoButton;
    @FXML
    private Button redoButton;
    @FXML
    private Button deleteHabitButton;
    @FXML
    private VBox aiSuggestionBox;
    @FXML
    private TextArea aiSuggestionsTextArea;
    @FXML private ProgressBar overallProgressBar;
    @FXML private Label progressPercentageLabel;
    @FXML private Label streakLabel;
    @FXML private Label longestStreakLabel;

    private HabitService habitService;
    private GeminiAIService aiService;
    private UserSession userSession;
    private final ObservableList<Habit> observableHabitsList = FXCollections.observableArrayList();
    private final java.util.Stack<Habit> undoStack = new java.util.Stack<>();
    private final java.util.Stack<Habit> redoStack = new java.util.Stack<>();

    public void initialize() {
        Connection conn = DatabaseConnector.getConnection();
        if (conn == null) {
            statusLabel.setText("Error: Database connection failed.");
            statusLabel.setStyle("-fx-text-fill: red;");
            markCompleteButton.setDisable(true);
            refreshButton.setDisable(true);
            return;
        }        habitService = new HabitService(conn);
        userSession = UserSession.getInstance();
        aiService = new GeminiAIService();
        aiSuggestionBox.setVisible(false);

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
                        streak = habitService.getCurrentStreak(habit);
                    }
                    String desc = (habit.getDescription() != null && !habit.getDescription().isEmpty()) ? "\n  └ " + habit.getDescription() : "";
                    setText(habit.getName() + desc + "\n  └ Streak: " + streak + " day" + (streak == 1 ? "" : "s"));
                }
            }
        });

        habitsListView.setItems(observableHabitsList);
        loadHabits();

        deleteHabitButton.setOnAction(e -> handleDeleteHabitAction());
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
        // Save to undo stack before marking complete
        undoStack.push(selectedHabit);
        redoStack.clear();
        LocalDate completionDate = LocalDate.now();
        boolean success = habitService.logHabitCompletion(selectedHabit.getId(), completionDate);
        if (success) {
            statusLabel.setText("Habit '" + selectedHabit.getName() + "' marked as complete for today!");
            statusLabel.setStyle("-fx-text-fill: green;");
        } else {
            statusLabel.setText("Failed to mark habit complete. Already logged for today or database error.");
            statusLabel.setStyle("-fx-text-fill: red;");
        }
    }

    @FXML
    protected void handleUndoAction() {
        if (!undoStack.isEmpty()) {
            Habit last = undoStack.pop();
            // Remove today's completion (implement in HabitService if not present)
            boolean undone = habitService.undoHabitCompletion(last.getId(), LocalDate.now());
            if (undone) {
                redoStack.push(last);
                statusLabel.setText("Undo: Marked '" + last.getName() + "' as incomplete for today.");
                statusLabel.setStyle("-fx-text-fill: orange;");
            } else {
                statusLabel.setText("Nothing to undo for '" + last.getName() + "'.");
            }
        }
    }

    @FXML
    protected void handleRedoAction() {
        if (!redoStack.isEmpty()) {
            Habit last = redoStack.pop();
            boolean redone = habitService.logHabitCompletion(last.getId(), LocalDate.now());
            if (redone) {
                undoStack.push(last);
                statusLabel.setText("Redo: Marked '" + last.getName() + "' as complete for today.");
                statusLabel.setStyle("-fx-text-fill: green;");
            }
        }
    }    @FXML
    protected void handleDeleteHabitAction() {
        Habit selectedHabit = habitsListView.getSelectionModel().getSelectedItem();
        if (selectedHabit == null) {
            statusLabel.setText("Please select a habit to delete.");
            statusLabel.setStyle("-fx-text-fill: orange;");
            return;
        }
        
        // Show confirmation dialog
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirm Deletion");
        confirmDialog.setHeaderText("Delete Habit: " + selectedHabit.getName());
        confirmDialog.setContentText("Are you sure you want to delete this habit? This will also remove all completion records and cannot be undone.");
        
        // Wait for user's confirmation
        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    boolean deleted = habitService.deleteHabit(selectedHabit.getId());
                    if (deleted) {
                        statusLabel.setText("Habit '" + selectedHabit.getName() + "' deleted.");
                        statusLabel.setStyle("-fx-text-fill: green;");
                        loadHabits();
                    } else {
                        statusLabel.setText("Failed to delete habit. Database error.");
                        statusLabel.setStyle("-fx-text-fill: red;");
                    }
                } catch (Exception e) {
                    statusLabel.setText("Error deleting habit: " + e.getMessage());
                    statusLabel.setStyle("-fx-text-fill: red;");
                    e.printStackTrace();
                }
            } else {
                statusLabel.setText("Deletion canceled.");
            }
        });
    }

    /**
     * Handler for AI habit suggestions button.
     * Uses existing habits to generate personalized suggestions.
     */
    @FXML
    private void handleAISuggestionAction() {
        if (!userSession.isLoggedIn()) {
            statusLabel.setText("Error: User not logged in.");
            return;
        }
        
        List<Habit> habits = habitService.getHabits(userSession.getUserId());
        if (habits.isEmpty()) {
            aiSuggestionsTextArea.setText("You don't have any habits yet. Try adding some first!");
            aiSuggestionBox.setVisible(true);
            return;
        }
        
        // Build a description of existing habits for the AI
        StringBuilder habitDescriptions = new StringBuilder();
        habits.forEach(habit -> {
            habitDescriptions.append("- ").append(habit.getName());
            if (habit.getDescription() != null && !habit.getDescription().trim().isEmpty()) {
                habitDescriptions.append(" (").append(habit.getDescription()).append(")");
            }
            habitDescriptions.append("\n");
        });
        
        // Default goals - in a real app, these could come from user settings
        String userGoals = "healthy lifestyle, productivity, personal growth";
        
        statusLabel.setText("Generating AI habit suggestions...");
        aiSuggestionsTextArea.setText("Thinking...");
        aiSuggestionBox.setVisible(true);
        
        // Generate suggestions asynchronously
        CompletableFuture.supplyAsync(() -> {
            try {
                return aiService.generateHabitSuggestions(habitDescriptions.toString(), userGoals);
            } catch (Exception e) {
                e.printStackTrace();
                return "Error generating suggestions: " + e.getMessage();
            }
        }).thenAccept(suggestions -> {
            Platform.runLater(() -> {
                aiSuggestionsTextArea.setText(suggestions);
                statusLabel.setText("");
            });
        });
    }
    
    /**
     * Handler to close the AI suggestions box.
     */
    @FXML
    private void handleCloseAISuggestions() {
        aiSuggestionBox.setVisible(false);
    }
    
    @FXML
    private void handleAddHabitAction() {}
    @FXML
    private void handleEditHabitAction() {}
    @FXML
    private void handleAddAISuggestion() {
        // TODO: Implement logic to add the selected AI suggestion as a new habit
    }
}
