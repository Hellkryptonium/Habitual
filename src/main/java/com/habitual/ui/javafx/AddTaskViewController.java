package com.habitual.ui.javafx;

import com.habitual.db.DatabaseConnector;
import com.habitual.schedule.ScheduleService;
import com.habitual.utils.ValidationUtils;
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

        // Reset any previous error styling
        ValidationUtils.setErrorStyle(taskNameField, false);
        ValidationUtils.setErrorStyle(taskDueDatepicker, false);

        // Validate task name
        String nameError = ValidationUtils.validateNotEmpty(taskNameField, "Task name");
        if (nameError != null) {
            statusLabel.setText(nameError);
            statusLabel.setStyle("-fx-text-fill: red;");
            ValidationUtils.setErrorStyle(taskNameField, true);
            return;
        }

        // Validate task name length
        String lengthError = ValidationUtils.validateLength(taskName, 1, 100, "Task name");
        if (lengthError != null) {
            statusLabel.setText(lengthError);
            statusLabel.setStyle("-fx-text-fill: red;");
            ValidationUtils.setErrorStyle(taskNameField, true);
            return;
        }

        // Validate due date
        String dateError = ValidationUtils.validateDateSelected(taskDueDatepicker, "due date");
        if (dateError != null) {
            statusLabel.setText(dateError);
            statusLabel.setStyle("-fx-text-fill: red;");
            ValidationUtils.setErrorStyle(taskDueDatepicker, true);
            return;
        }

        // Check SQL injection security
        String sqlError = ValidationUtils.validateNoSQLInjection(taskName, "Task name");
        if (sqlError != null) {
            statusLabel.setText(sqlError);
            statusLabel.setStyle("-fx-text-fill: red;");
            ValidationUtils.setErrorStyle(taskNameField, true);
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
                LocalTime dueTime = LocalTime.parse(dueTimeStr.trim(), DateTimeFormatter.ofPattern("HH:mm"));
                dueDateTime = LocalDateTime.of(dueDate, dueTime);
            } catch (DateTimeParseException e) {
                statusLabel.setText("Invalid time format. Please use HH:mm (e.g., 14:30).");
                statusLabel.setStyle("-fx-text-fill: red;");
                return;
            }
        } else {
            dueDateTime = LocalDateTime.of(dueDate, LocalTime.MIDNIGHT);
        }
        Timestamp dueTimestamp = Timestamp.valueOf(dueDateTime);
        int defaultPriority = 1;

        try {
            boolean success = scheduleService.addTask(userSession.getUserId(), taskName.trim(), description, dueTimestamp, defaultPriority);

            if (success) {
                statusLabel.setText("Task '" + taskName + "' added successfully!");
                statusLabel.setStyle("-fx-text-fill: green;");
                taskNameField.clear();
                taskDescriptionArea.clear();
                taskDueTimeField.clear();
                taskDueDatepicker.setValue(LocalDate.now()); // Reset to today
                taskNameField.requestFocus(); // Set focus for next entry
            } else {
                statusLabel.setText("Failed to add task. It might be a duplicate or database issue.");
                statusLabel.setStyle("-fx-text-fill: red;");
            }
        } catch (Exception e) {
            statusLabel.setText("Error adding task: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: red;");
            e.printStackTrace();
        }
    }
}
