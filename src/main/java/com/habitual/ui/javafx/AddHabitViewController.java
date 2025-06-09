package com.habitual.ui.javafx;

import com.habitual.db.DatabaseConnector;
import com.habitual.habits.HabitService;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.sql.Connection;

public class AddHabitViewController {

    @FXML
    private TextField habitNameField;
    @FXML
    private TextArea habitDescriptionArea;
    @FXML
    private Button saveHabitButton;
    @FXML
    private Label statusLabel;

    private HabitService habitService;
    private UserSession userSession;

    public void initialize() {
        Connection conn = DatabaseConnector.getConnection();
        if (conn == null) {
            statusLabel.setText("Error: Database connection failed.");
            statusLabel.setStyle("-fx-text-fill: red;");
            // Disable form if no DB connection
            habitNameField.setDisable(true);
            habitDescriptionArea.setDisable(true);
            saveHabitButton.setDisable(true);
            return;
        }
        habitService = new HabitService(conn);
        userSession = UserSession.getInstance();
        statusLabel.setText(""); // Clear any previous status

        // Real-time validation for habit name
        habitNameField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                saveHabitButton.setDisable(true);
                // Only show error if field is empty and not focused
                if (!habitNameField.isFocused()) {
                    statusLabel.setText("Habit name cannot be empty.");
                    statusLabel.setStyle("-fx-text-fill: red;");
                } else {
                    statusLabel.setText("");
                }
            } else {
                statusLabel.setText("");
                saveHabitButton.setDisable(false);
            }
        });
    }    @FXML
    protected void handleSaveHabitAction() {
        String habitName = habitNameField.getText();
        String description = habitDescriptionArea.getText();
        
        // Enhanced validation
        if (habitName == null || habitName.trim().isEmpty()) {
            statusLabel.setText("Habit name cannot be empty.");
            statusLabel.setStyle("-fx-text-fill: red;");
            habitNameField.requestFocus();
            return;
        }
        
        // Validate name length
        if (habitName.trim().length() > 100) {
            statusLabel.setText("Habit name too long (max 100 characters).");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }
        
        // Check for invalid characters that might cause SQL problems
        if (habitName.contains("'") || habitName.contains("\"") || habitName.contains(";")) {
            statusLabel.setText("Habit name contains invalid characters (' \" ;).");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        if (!userSession.isLoggedIn()) {
            statusLabel.setText("Error: No user logged in. Please log in again.");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        try {
            String defaultFrequency = "Daily"; // Default frequency
            boolean success = habitService.addHabit(userSession.getUserId(), habitName.trim(), description, defaultFrequency, java.time.LocalDate.now());

            if (success) {
                statusLabel.setText("Habit '" + habitName + "' added successfully!");
                statusLabel.setStyle("-fx-text-fill: green;");
                // Clear fields after successful save
                habitNameField.clear();
                habitDescriptionArea.clear();
                // Set focus back to name field for next entry
                habitNameField.requestFocus();
                // Disable save button until new input
                saveHabitButton.setDisable(true);
            } else {
                statusLabel.setText("Failed to add habit. It might already exist.");
                statusLabel.setStyle("-fx-text-fill: red;");
            }
        } catch (Exception e) {
            statusLabel.setText("Error: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: red;");
            e.printStackTrace();
        }
    }
}
