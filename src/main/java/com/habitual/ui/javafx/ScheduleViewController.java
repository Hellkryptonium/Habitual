package com.habitual.ui.javafx;

import com.habitual.db.DatabaseConnector;
import com.habitual.schedule.ScheduleService;
import com.habitual.schedule.ScheduledTask;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

import java.sql.Connection;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ScheduleViewController {

    @FXML
    private ListView<ScheduledTask> tasksListView;
    @FXML
    private Button markTaskCompleteButton;
    @FXML
    private Button refreshTasksButton;
    @FXML
    private Label statusLabel;

    private ScheduleService scheduleService;
    private UserSession userSession;
    private final ObservableList<ScheduledTask> observableTasksList = FXCollections.observableArrayList();
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public void initialize() {
        Connection conn = DatabaseConnector.getConnection();
        if (conn == null) {
            statusLabel.setText("Error: Database connection failed.");
            statusLabel.setStyle("-fx-text-fill: red;");
            markTaskCompleteButton.setDisable(true);
            refreshTasksButton.setDisable(true);
            return;
        }
        scheduleService = new ScheduleService(conn);
        userSession = UserSession.getInstance();

        tasksListView.setCellFactory(param -> new ListCell<ScheduledTask>() {
            @Override
            protected void updateItem(ScheduledTask task, boolean empty) {
                super.updateItem(task, empty);
                if (empty || task == null || task.getTitle() == null) { // Changed getTaskName to getTitle
                    setText(null);
                } else {
                    String dueDateTimeStr = task.getDueDate() != null ? task.getDueDate().toLocalDateTime().format(dateTimeFormatter) : "No due date";
                    String status = task.isCompleted() ? " [COMPLETED]" : "";
                    setText(task.getTitle() + status + "\n  └ Due: " + dueDateTimeStr + // Changed getTaskName to getTitle
                            (task.getDescription() != null && !task.getDescription().isEmpty() ? "\n  └ Desc: " + task.getDescription() : ""));
                    if (task.isCompleted()) {
                        setStyle("-fx-text-fill: green;");
                    } else if (task.getDueDate() != null && task.getDueDate().before(new Timestamp(System.currentTimeMillis()))) {
                        setStyle("-fx-text-fill: orange;"); // Overdue
                    } else {
                        setStyle(""); // Reset style
                    }
                }
            }
        });

        tasksListView.setItems(observableTasksList);
        loadTasks();
    }

    @FXML
    protected void loadTasks() {
        if (!userSession.isLoggedIn()) {
            statusLabel.setText("User not logged in.");
            statusLabel.setStyle("-fx-text-fill: red;");
            observableTasksList.clear();
            return;
        }
        try {
            // Assuming getTasks returns tasks ordered by due date
            List<ScheduledTask> userTasks = scheduleService.getTasks(userSession.getUserId(), true); // Added true for includeCompleted
            observableTasksList.setAll(userTasks);
            if (userTasks.isEmpty()) {
                statusLabel.setText("No tasks found. Add some!");
                statusLabel.setStyle("-fx-text-fill: orange;");
            } else {
                statusLabel.setText("Tasks loaded.");
                statusLabel.setStyle("-fx-text-fill: green;");
            }
        } catch (Exception e) {
            statusLabel.setText("Error loading tasks: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: red;");
            e.printStackTrace();
        }
    }

    @FXML
    protected void handleMarkTaskCompleteAction() {
        ScheduledTask selectedTask = tasksListView.getSelectionModel().getSelectedItem();
        if (selectedTask == null) {
            statusLabel.setText("Please select a task to mark as complete.");
            statusLabel.setStyle("-fx-text-fill: orange;");
            return;
        }

        if (selectedTask.isCompleted()) {
            statusLabel.setText("Task '" + selectedTask.getTitle() + "' is already marked as complete."); // Changed getTaskName to getTitle
            statusLabel.setStyle("-fx-text-fill: blue;");
            return;
        }

        boolean success = scheduleService.markTaskComplete(selectedTask.getId(), true); // Added true for isCompleted

        if (success) {
            statusLabel.setText("Task '" + selectedTask.getTitle() + "' marked as complete!"); // Changed getTaskName to getTitle
            loadTasks(); // Refresh the list to show updated status
        } else {
            statusLabel.setText("Failed to mark task complete. Database error occurred.");
            statusLabel.setStyle("-fx-text-fill: red;");
        }
    }
}
