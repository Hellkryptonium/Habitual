package com.habitual.ui.javafx;

import com.habitual.auth.AuthService;
import com.habitual.db.DatabaseConnector;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import java.io.IOException;
import java.sql.Connection;

public class LoginViewController {

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button loginButton;
    @FXML
    private Button registerButton;
    @FXML
    private Button forgotPasswordButton;
    @FXML
    private Label errorLabel;

    private AuthService authService;

    public void initialize() {
        Connection conn = DatabaseConnector.getConnection();
        if (conn == null) {
            errorLabel.setText("Error: Database connection failed.");
            // Disable form if no DB connection
            usernameField.setDisable(true);
            passwordField.setDisable(true);
            loginButton.setDisable(true);
            registerButton.setDisable(true);
            forgotPasswordButton.setDisable(true);
            return;
        }
        authService = new AuthService(conn);
        errorLabel.setText(""); // Clear any previous errors
    }    @FXML
    protected void handleLoginButtonAction() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        // Enhanced validation
        if (username == null || username.trim().isEmpty()) {
            errorLabel.setText("Username cannot be empty.");
            usernameField.requestFocus();
            return;
        }

        if (password == null || password.isEmpty()) {
            errorLabel.setText("Password cannot be empty.");
            passwordField.requestFocus();
            return;
        }
        
        // Disable the login button to prevent multiple clicks
        loginButton.setDisable(true);
        errorLabel.setText("Logging in...");
        
        try {
            if (authService.loginUser(username, password)) {
                // Navigate to the main dashboard
                try {
                    // Store user ID for the session
                    int userId = authService.getUserIdByUsername(username);
                    UserSession.getInstance().setLoggedInUser(username, userId);
                    MainFxApp.loadScene("MainDashboardView.fxml", "Habitual - Dashboard");
                } catch (IOException e) {
                    errorLabel.setText("Error: Could not load dashboard. " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                errorLabel.setText("Login failed. Invalid username or password.");
                // Highlight fields for better UX
                passwordField.clear();
                passwordField.requestFocus();
            }
        } catch (Exception e) {
            errorLabel.setText("Login error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Re-enable the login button
            loginButton.setDisable(false);
        }
    }

    @FXML
    protected void handleRegisterNavigation() {
        try {
            MainFxApp.loadScene("RegisterView.fxml", "Habitual - Register");
        } catch (IOException e) {
            errorLabel.setText("Error: Could not load registration page.");
            e.printStackTrace();
        }
    }

    @FXML
    protected void handleForgotPassword() {
        try {
            MainFxApp.loadScene("ForgotPasswordView.fxml", "Habitual - Reset Password");
        } catch (IOException e) {
            errorLabel.setText("Error loading password reset screen.");
        }
    }
}
