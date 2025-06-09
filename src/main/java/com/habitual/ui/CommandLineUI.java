package com.habitual.ui;

import com.habitual.auth.AuthService;
import com.habitual.db.DatabaseConnector;
import com.habitual.habits.Habit;
import com.habitual.habits.HabitService;
import com.habitual.schedule.ScheduleService;
import com.habitual.schedule.ScheduledTask;

import java.sql.Connection;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Scanner;

public class CommandLineUI {
    private Scanner scanner;
    private AuthService authService;
    private HabitService habitService;
    private ScheduleService scheduleService;
    private String loggedInUsername = null; // Stores the username of the logged-in user
    private int loggedInUserId = -1;      // Stores the ID of the logged-in user

    public CommandLineUI() {
        this.scanner = new Scanner(System.in);
        Connection conn = DatabaseConnector.getConnection();
        if (conn == null) {
            System.err.println("Failed to establish database connection. Exiting.");
            System.exit(1);
        }
        this.authService = new AuthService(conn);
        this.habitService = new HabitService(conn);
        this.scheduleService = new ScheduleService(conn);
    }

    public void start() {
        System.out.println("Welcome to Habitual - Your Habit Tracker and Schedule Maker!");

        DatabaseConnector.initializeDatabase();

        while (true) {
            if (loggedInUsername == null) {
                showAuthMenu();
            } else {
                showMainMenu();
            }
        }
    }

    private void showAuthMenu() {
        System.out.println("\n--- Authentication ---");
        System.out.println("1. Register");
        System.out.println("2. Login");
        System.out.println("3. Exit");
        System.out.print("Choose an option: ");
        String choice = scanner.nextLine();

        switch (choice) {
            case "1":
                handleRegister();
                break;
            case "2":
                handleLogin();
                break;
            case "3":
                System.out.println("Exiting Habitual. Goodbye!");
                DatabaseConnector.closeConnection(); // Close DB connection on exit
                System.exit(0);
                break;
            default:
                System.out.println("Invalid option. Please try again.");
        }
    }

    private void handleRegister() {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();
        if (authService.registerUser(username, password)) {
            System.out.println("Registration successful!");
        } else {
            System.out.println("Registration failed. Username might already exist.");
        }
    }

    private void handleLogin() {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();
        if (authService.loginUser(username, password)) {
            loggedInUsername = username;
            loggedInUserId = authService.getUserIdByUsername(username); // Get and store user ID
            if (loggedInUserId == -1) {
                System.out.println("Login failed: Could not retrieve user ID.");
                loggedInUsername = null; // Reset username if ID fetch fails
                return;
            }
            System.out.println("Login successful. Welcome, " + loggedInUsername + "!");
        } else {
            System.out.println("Login failed. Invalid username or password.");
        }
    }

    private void showMainMenu() {
        System.out.println("\n--- Main Menu (Logged in as: " + loggedInUsername + ") ---");
        System.out.println("1. Habit Tracker");
        System.out.println("2. Schedule Maker");
        System.out.println("3. Logout");
        System.out.println("4. Exit");
        System.out.print("Choose an option: ");
        String choice = scanner.nextLine();

        switch (choice) {
            case "1":
                showHabitMenu();
                break;
            case "2":
                showScheduleMenu();
                break;
            case "3":
                loggedInUsername = null;
                loggedInUserId = -1; // Reset user ID on logout
                System.out.println("Logged out successfully.");
                break;
            case "4":
                System.out.println("Exiting Habitual. Goodbye!");
                DatabaseConnector.closeConnection(); // Close DB connection on exit
                System.exit(0);
                break;
            default:
                System.out.println("Invalid option. Please try again.");
        }
    }

    private void showHabitMenu() {
        System.out.println("\n--- Habit Tracker ---");
        System.out.println("1. Add Habit");
        System.out.println("2. View Habits");
        System.out.println("3. Log Habit Completion");
        System.out.println("4. Back to Main Menu");
        System.out.print("Choose an option: ");
        String choice = scanner.nextLine();

        switch (choice) {
            case "1":
                handleAddHabit();
                break;
            case "2":
                handleViewHabits();
                break;
            case "3":
                handleLogHabitCompletion();
                break;
            case "4":
                return;
            default:
                System.out.println("Invalid option.");
        }
    }

    private void handleAddHabit() {
        System.out.print("Enter habit name: ");
        String name = scanner.nextLine();
        System.out.print("Enter habit description (optional): ");
        String description = scanner.nextLine();
        System.out.print("Enter habit frequency (e.g., daily, weekly): ");
        String frequency = scanner.nextLine();

        if (habitService.addHabit(loggedInUserId, name, description, frequency, java.time.LocalDate.now())) {
            System.out.println("Habit added successfully!");
        } else {
            System.out.println("Failed to add habit.");
        }
    }

    private void handleViewHabits() {
        List<Habit> habits = habitService.getHabits(loggedInUserId);
        if (habits.isEmpty()) {
            System.out.println("No habits found.");
            return;
        }
        System.out.println("\n--- Your Habits ---");
        for (Habit habit : habits) {
            System.out.println(habit); // Assumes Habit class has a decent toString()
        }
    }

    private void handleLogHabitCompletion() {
        handleViewHabits(); // Show habits to help user choose
        System.out.print("Enter ID of the habit to log completion for: ");
        String habitIdStr = scanner.nextLine();
        int habitId;
        try {
            habitId = Integer.parseInt(habitIdStr);
        } catch (NumberFormatException e) {
            System.out.println("Invalid habit ID format.");
            return;
        }

        System.out.print("Enter completion date (YYYY-MM-DD): ");
        String dateStr = scanner.nextLine();
        // Basic validation for date format can be added here
        try {
            java.time.LocalDate completionDate = java.time.LocalDate.parse(dateStr);
            if (habitService.logHabitCompletion(habitId, completionDate)) {
                System.out.println("Habit completion logged successfully!");
            } else {
                System.out.println("Failed to log habit completion. Ensure habit ID is correct and date is unique for this habit.");
            }
        } catch (Exception e) {
            System.out.println("Invalid date format. Please use YYYY-MM-DD.");
        }
    }

    private void showScheduleMenu() {
        System.out.println("\n--- Schedule Maker ---");
        System.out.println("1. Add Task");
        System.out.println("2. View Schedule");
        System.out.println("3. Mark Task Status");
        System.out.println("4. Back to Main Menu");
        System.out.print("Choose an option: ");
        String choice = scanner.nextLine();

        switch (choice) {
            case "1":
                handleAddTask();
                break;
            case "2":
                handleViewSchedule();
                break;
            case "3":
                handleMarkTaskStatus();
                break;
            case "4":
                return;
            default:
                System.out.println("Invalid option.");
        }
    }

    private void handleAddTask() {
        System.out.print("Enter task title: ");
        String title = scanner.nextLine();
        System.out.print("Enter task description (optional): ");
        String description = scanner.nextLine();
        System.out.print("Enter due date (YYYY-MM-DD HH:MM:SS or YYYY-MM-DD): ");
        String dueDateStr = scanner.nextLine();
        Timestamp dueDate = null;
        try {
            // Attempt to parse with time, then without if it fails
            SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            if (dueDateStr.contains(":")) {
                dueDate = new Timestamp(dateTimeFormat.parse(dueDateStr).getTime());
            } else {
                dueDate = new Timestamp(dateFormat.parse(dueDateStr).getTime());
            }
        } catch (ParseException e) {
            System.out.println("Invalid date format. Please use YYYY-MM-DD HH:MM:SS or YYYY-MM-DD.");
            return;
        }

        System.out.print("Enter priority (1=High, 2=Medium, 3=Low, default 3): ");
        String priorityStr = scanner.nextLine();
        int priority = 3;
        if (!priorityStr.isEmpty()) {
            try {
                priority = Integer.parseInt(priorityStr);
            } catch (NumberFormatException e) {
                System.out.println("Invalid priority format, using default (Low).");
            }
        }

        if (scheduleService.addTask(loggedInUserId, title, description, dueDate, priority)) {
            System.out.println("Task added successfully!");
        } else {
            System.out.println("Failed to add task.");
        }
    }

    private void handleViewSchedule() {
        System.out.print("Include completed tasks? (yes/no, default no): ");
        String includeCompletedStr = scanner.nextLine().trim().toLowerCase();
        boolean includeCompleted = includeCompletedStr.equals("yes");

        List<ScheduledTask> tasks = scheduleService.getTasks(loggedInUserId, includeCompleted);
        if (tasks.isEmpty()) {
            System.out.println("No tasks found.");
            return;
        }
        System.out.println("\n--- Your Schedule ---");
        for (ScheduledTask task : tasks) {
            System.out.println(task); // Assumes ScheduledTask class has a decent toString()
        }
    }

    private void handleMarkTaskStatus() {
        handleViewSchedule(); // Show tasks to help user choose
        System.out.print("Enter ID of the task to mark: ");
        String taskIdStr = scanner.nextLine();
        int taskId;
        try {
            taskId = Integer.parseInt(taskIdStr);
        } catch (NumberFormatException e) {
            System.out.println("Invalid task ID format.");
            return;
        }

        System.out.print("Mark as complete? (yes/no): ");
        String completeStr = scanner.nextLine().trim().toLowerCase();
        boolean isCompleted = completeStr.equals("yes");

        if (scheduleService.markTaskComplete(taskId, isCompleted)) {
            System.out.println("Task status updated successfully!");
        } else {
            System.out.println("Failed to update task status. Ensure task ID is correct.");
        }
    }
}
