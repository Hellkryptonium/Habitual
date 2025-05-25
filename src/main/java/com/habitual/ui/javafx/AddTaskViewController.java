package com.habitual.ui.javafx;

import com.habitual.db.DatabaseConnector;
import com.habitual.schedule.ScheduleService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.sql.Connection;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class AddTaskViewController {

    @FXML
    private TextField taskNameField;
    @FXML
    private TextArea taskDescriptionArea;
    @FXML
    private DatePicker taskDueDatepicker;
    @FXML
    private TextField taskDueTimeField; // For HH:MM input
    @FXML
    private Button saveTaskButton;
    @FXML
    private Label statusLabel;

    private ScheduleService scheduleService;
    private UserSession userSession;

    public void initialize() {
        Connection conn = DatabaseConnector.getConnection();
        if (conn == null) {
            statusLabel.setText("Error: Database connection failed.");
            statusLabel.setStyle("-fx-text-fill: red;");
            taskNameField.setDisable(true);
            taskDescriptionArea.setDisable(true);
            taskDueDatepicker.setDisable(true);
            taskDueTimeField.setDisable(true);
            saveTaskButton.setDisable(true);
            return;
        }
        scheduleService = new ScheduleService(conn);
        userSession = UserSession.getInstance();
        statusLabel.setText(""); // Clear status
        taskDueDatepicker.setValue(LocalDate.now()); // Default to today
    }

    @FXML
    protected void handleSaveTaskAction() {
        String taskName = taskNameField.getText();
        String description = taskDescriptionArea.getText();
        LocalDate dueDate = taskDueDatepicker.getValue();
        String dueTimeStr = taskDueTimeField.getText();

        if (taskName.isEmpty()) {
            statusLabel.setText("Task name cannot be empty.");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }
        if (dueDate == null) {
            statusLabel.setText("Due date must be selected.");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        if (!userSession.isLoggedIn()) {
            statusLabel.setText("Error: No user logged in. Please log in again.");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        LocalDateTime dueDateTime;
        if (dueTimeStr != null && !dueTimeStr.trim().isEmpty()) {
            try {
                LocalTime dueTime = LocalTime.parse(dueTimeStr, DateTimeFormatter.ofPattern("HH:mm"));
                dueDateTime = LocalDateTime.of(dueDate, dueTime);
            } catch (DateTimeParseException e) {
                statusLabel.setText("Invalid time format. Please use HH:mm (e.g., 14:30).");
                statusLabel.setStyle("-fx-text-fill: red;");
                return;
            }
        } else {
            // If no time is specified, default to end of day or a specific time like 00:00
            dueDateTime = LocalDateTime.of(dueDate, LocalTime.MIDNIGHT); // Or LocalTime.MAX for end of day
        }

        Timestamp dueTimestamp = Timestamp.valueOf(dueDateTime);
        int defaultPriority = 1; // Default priority

        boolean success = scheduleService.addTask(userSession.getUserId(), taskName, description, dueTimestamp, defaultPriority);

        if (success) {
            statusLabel.setText("Task '" + taskName + "' added successfully!");
            statusLabel.setStyle("-fx-text-fill: green;");
            taskNameField.clear();
            taskDescriptionArea.clear();
            taskDueDatepicker.setValue(LocalDate.now());
            taskDueTimeField.clear();
        } else {
            statusLabel.setText("Failed to add task. Please check details or try again.");
            statusLabel.setStyle("-fx-text-fill: red;");
        }
    }
}
