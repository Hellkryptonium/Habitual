package com.habitual.ui.javafx;

import com.habitual.auth.AuthService;
import com.habitual.db.DatabaseConnector;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.sql.Connection;

public class ChangeSecurityQuestionViewController {
    @FXML private PasswordField currentPasswordField;
    @FXML private TextField newSecurityQuestionField;
    @FXML private TextField newSecurityAnswerField;
    @FXML private Button updateButton;
    @FXML private Label statusLabel;

    private AuthService authService;
    private UserSession userSession;

    public void initialize() {
        Connection conn = DatabaseConnector.getConnection();
        authService = new AuthService(conn);
        userSession = UserSession.getInstance();
        statusLabel.setText("");
    }

    @FXML
    protected void handleUpdateSecurityQuestion() {
        String password = currentPasswordField.getText();
        String newQuestion = newSecurityQuestionField.getText();
        String newAnswer = newSecurityAnswerField.getText();
        if (password.isEmpty() || newQuestion.isEmpty() || newAnswer.isEmpty()) {
            statusLabel.setText("All fields are required.");
            return;
        }
        String username = userSession.getUsername();
        if (!authService.loginUser(username, password)) {
            statusLabel.setText("Incorrect password.");
            return;
        }
        boolean success = authService.updateSecurityQuestion(username, newQuestion, newAnswer);
        if (success) {
            statusLabel.setText("Security question updated successfully!");
        } else {
            statusLabel.setText("Failed to update security question.");
        }
    }
}
