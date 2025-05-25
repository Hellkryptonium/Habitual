package com.habitual.ui.javafx;

import com.habitual.auth.AuthService;
import com.habitual.db.DatabaseConnector;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.sql.Connection;

public class ForgotPasswordViewController {
    @FXML private TextField usernameField;
    @FXML private Button fetchQuestionButton;
    @FXML private Label securityQuestionLabel;
    @FXML private TextField answerField;
    @FXML private PasswordField newPasswordField;
    @FXML private Button resetPasswordButton;
    @FXML private Label statusLabel;
    @FXML private Button backToLoginButton;

    private AuthService authService;
    private String currentUsername;

    public void initialize() {
        Connection conn = DatabaseConnector.getConnection();
        authService = new AuthService(conn);
        securityQuestionLabel.setText("");
        statusLabel.setText("");
    }

    @FXML
    protected void handleFetchQuestion() {
        String username = usernameField.getText();
        if (username.isEmpty()) {
            statusLabel.setText("Please enter your username.");
            return;
        }
        String question = authService.getSecurityQuestion(username);
        if (question != null && !question.isEmpty()) {
            securityQuestionLabel.setText(question);
            currentUsername = username;
            statusLabel.setText("");
        } else {
            securityQuestionLabel.setText("");
            statusLabel.setText("No such user or no security question set.");
        }
    }

    @FXML
    protected void handleResetPassword() {
        if (currentUsername == null || currentUsername.isEmpty()) {
            statusLabel.setText("Please fetch your security question first.");
            return;
        }
        String answer = answerField.getText();
        String newPassword = newPasswordField.getText();
        if (answer.isEmpty() || newPassword.isEmpty()) {
            statusLabel.setText("Please answer the question and enter a new password.");
            return;
        }
        if (!authService.verifySecurityAnswer(currentUsername, answer)) {
            statusLabel.setText("Incorrect answer to security question.");
            return;
        }
        boolean success = authService.resetPassword(currentUsername, newPassword);
        if (success) {
            statusLabel.setText("Password reset successful! You can now log in.");
        } else {
            statusLabel.setText("Failed to reset password. Try again.");
        }
    }

    @FXML
    protected void handleBackToLogin() {
        try {
            MainFxApp.loadScene("LoginView.fxml", "Habitual - Login");
        } catch (Exception e) {
            statusLabel.setText("Error returning to login.");
        }
    }
}
