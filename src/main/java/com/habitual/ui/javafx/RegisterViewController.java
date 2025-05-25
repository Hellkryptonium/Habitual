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

public class RegisterViewController {

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private Button registerButton;
    @FXML
    private Button backToLoginButton;
    @FXML
    private Label errorLabel;
    @FXML
    private TextField securityQuestionField;
    @FXML
    private TextField securityAnswerField;

    private AuthService authService;

    public void initialize() {
        Connection conn = DatabaseConnector.getConnection();
        if (conn == null) {
            errorLabel.setText("Error: Database connection failed.");
            // Disable form if no DB connection
            usernameField.setDisable(true);
            passwordField.setDisable(true);
            confirmPasswordField.setDisable(true);
            registerButton.setDisable(true);
            backToLoginButton.setDisable(true);
            return;
        }
        authService = new AuthService(conn);
        errorLabel.setText(""); // Clear any previous errors
    }

    @FXML
    protected void handleRegisterButtonAction() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String securityQuestion = securityQuestionField.getText();
        String securityAnswer = securityAnswerField.getText();

        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || securityQuestion.isEmpty() || securityAnswer.isEmpty()) {
            errorLabel.setText("All fields are required.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            errorLabel.setText("Passwords do not match.");
            return;
        }

        // TODO: Add more validation for username and password (e.g., length, complexity)

        boolean success = authService.registerUser(username, password, securityQuestion, securityAnswer);

        if (success) {
            // Navigate to the login screen or directly to dashboard after registration
            try {
                // For now, navigate to login. Could also auto-login and go to dashboard.
                MainFxApp.loadScene("LoginView.fxml", "Habitual - Login");
            } catch (IOException e) {
                errorLabel.setText("Error: Could not load login page.");
                e.printStackTrace();
            }
        } else {
            // Check if the error is due to existing username (AuthService might need a way to signal this)
            // For now, a generic message.
            errorLabel.setText("Registration failed. Username might already exist or database error.");
        }
    }

    @FXML
    protected void handleBackToLoginNavigation() {
        try {
            MainFxApp.loadScene("LoginView.fxml", "Habitual - Login");
        } catch (IOException e) {
            errorLabel.setText("Error: Could not load login page.");
            e.printStackTrace();
        }
    }
}
