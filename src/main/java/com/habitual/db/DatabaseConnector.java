package com.habitual.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnector {
    // Replace with your actual database URL, user, and password
    private static final String DB_URL = "jdbc:mysql://localhost:3306/habitual_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String DB_USER = "root"; // TODO: Replace with your MySQL username
    private static final String DB_PASSWORD = "Harish@#123"; // TODO: Replace with your MySQL password
    private static Connection connection = null;

    public static Connection getConnection() {
        if (connection == null) {
            try {
                // Ensure the MySQL JDBC driver is loaded
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                System.out.println("Database connection established.");
            } catch (ClassNotFoundException e) {
                System.err.println("MySQL JDBC Driver not found.");
                e.printStackTrace();
                // Consider re-throwing as a runtime exception or handling more gracefully
            } catch (SQLException e) {
                System.err.println("Database connection failed.");
                e.printStackTrace();
                // Consider re-throwing as a runtime exception or handling more gracefully
            }
        }
        return connection;
    }

    public static void initializeDatabase() {
        Connection conn = getConnection(); // Get the shared connection
        if (conn == null) {
            System.err.println("Cannot initialize database - connection is null or failed to establish.");
            return;
        }
        // DO NOT use try-with-resources for 'conn' here as it's a shared connection
        // intended to live longer.
        // DO use try-with-resources for 'Statement'.
        try (Statement stmt = conn.createStatement()) {
            // Create users table
            String createUsersTable = "CREATE TABLE IF NOT EXISTS users ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY,"
                    + "username VARCHAR(50) NOT NULL UNIQUE,"
                    + "password_hash VARCHAR(255) NOT NULL,"
                    + "security_question VARCHAR(255)," // Added security question column
                    + "security_answer VARCHAR(255)" // Added security answer column
                    + ");";
            stmt.executeUpdate(createUsersTable);
            System.out.println("Users table checked/created.");

            // Create habits table
            String createHabitsTable = "CREATE TABLE IF NOT EXISTS habits ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY,"
                    + "user_id INT NOT NULL,"
                    + "name VARCHAR(100) NOT NULL,"
                    + "description TEXT,"
                    + "frequency VARCHAR(50)," // e.g., daily, weekly
                    + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                    + "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE"
                    + ");";
            stmt.executeUpdate(createHabitsTable);
            System.out.println("Habits table checked/created.");

            // Create habit_completions table
            String createHabitCompletionsTable = "CREATE TABLE IF NOT EXISTS habit_completions ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY,"
                    + "habit_id INT NOT NULL,"
                    + "completion_date DATE NOT NULL,"
                    + "notes TEXT,"
                    + "FOREIGN KEY (habit_id) REFERENCES habits(id) ON DELETE CASCADE,"
                    + "UNIQUE (habit_id, completion_date)"
                    + ");";
            stmt.executeUpdate(createHabitCompletionsTable);
            System.out.println("Habit completions table checked/created.");

            // Create scheduled_tasks table
            String createScheduledTasksTable = "CREATE TABLE IF NOT EXISTS scheduled_tasks ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY,"
                    + "user_id INT NOT NULL,"
                    + "title VARCHAR(100) NOT NULL,"
                    + "description TEXT,"
                    + "due_date TIMESTAMP,"
                    + "priority INT DEFAULT 3," // e.g., 1=High, 2=Medium, 3=Low
                    + "is_completed BOOLEAN DEFAULT FALSE,"
                    + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                    + "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE"
                    + ");";
            stmt.executeUpdate(createScheduledTasksTable);
            System.out.println("Scheduled tasks table checked/created.");

            System.out.println("Database schema initialized successfully.");

        } catch (SQLException e) {
            System.err.println("Error initializing database schema:");
            e.printStackTrace();
            // If an error occurs during schema initialization, the connection might be in a bad state.
            // It's safer to close the shared connection and let it be reopened on the next demand.
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException closeEx) {
                    System.err.println("Error closing connection after schema init failure: " + closeEx.getMessage());
                }
                connection = null; // Force re-creation on next call to getConnection()
            }
        }
        // The shared 'conn' (which is the static 'connection' field) is NOT closed here by a try-with-resources
        // on the connection itself, allowing it to be used by services.
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Database connection closed.");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}