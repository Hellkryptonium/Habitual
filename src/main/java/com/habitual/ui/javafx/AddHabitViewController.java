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
        habitNameField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (newValue.trim().isEmpty()) {
                    statusLabel.setText("Habit name cannot be empty.");
                    statusLabel.setStyle("-fx-text-fill: red;");
                    saveHabitButton.setDisable(true);
                } else {
                    statusLabel.setText("");
                    saveHabitButton.setDisable(false);
                }
            }
        });
    }

    @FXML
    protected void handleSaveHabitAction() {
        String habitName = habitNameField.getText();
        String description = habitDescriptionArea.getText();

        if (habitName.isEmpty()) {
            statusLabel.setText("Habit name cannot be empty.");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        if (!userSession.isLoggedIn()) {
            statusLabel.setText("Error: No user logged in. Please log in again.");
            statusLabel.setStyle("-fx-text-fill: red;");
            // Optionally, redirect to login
            return;
        }

        String defaultFrequency = "Daily"; // Default frequency

        boolean success = habitService.addHabit(userSession.getUserId(), habitName, description, defaultFrequency);

        if (success) {
            statusLabel.setText("Habit '" + habitName + "' added successfully!");
            statusLabel.setStyle("-fx-text-fill: green;");
            // Clear fields after successful save
            habitNameField.clear();
            habitDescriptionArea.clear();
        } else {
            statusLabel.setText("Failed to add habit. It might already exist or a database error occurred.");
            statusLabel.setStyle("-fx-text-fill: red;");
        }
    }
}
