package com.habitual.auth;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.mindrot.jbcrypt.BCrypt; // Import BCrypt

public class AuthService {
    private Connection dbConnection;

    public AuthService(Connection connection) {
        this.dbConnection = connection;
    }

    public boolean registerUser(String username, String password) {
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt()); // Hash the password
        String sql = "INSERT INTO users (username, password_hash) VALUES (?, ?)";
        try (PreparedStatement pstmt = dbConnection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, hashedPassword);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            // Handle specific SQL exceptions, e.g., duplicate username (SQLIntegrityConstraintViolationException)
            System.err.println("Error registering user: " + e.getMessage());
            return false;
        }
    }

    public boolean loginUser(String username, String password) {
        String sql = "SELECT password_hash FROM users WHERE username = ?";
        try (PreparedStatement pstmt = dbConnection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String storedHashedPassword = rs.getString("password_hash");
                return BCrypt.checkpw(password, storedHashedPassword); // Verify hashed password
            }
            return false; // User not found
        } catch (SQLException e) {
            System.err.println("Error logging in user: " + e.getMessage());
            return false;
        }
    }

    public int getUserIdByUsername(String username) {
        String sql = "SELECT id FROM users WHERE username = ?";
        try (PreparedStatement pstmt = dbConnection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            System.err.println("Error fetching user ID: " + e.getMessage());
        }
        return -1; // Return -1 if user not found or error occurs
    }

    public boolean registerUser(String username, String password, String securityQuestion, String securityAnswer) {
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        String sql = "INSERT INTO users (username, password_hash, security_question, security_answer) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = dbConnection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, hashedPassword);
            pstmt.setString(3, securityQuestion);
            pstmt.setString(4, securityAnswer);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error registering user: " + e.getMessage());
            return false;
        }
    }

    public String getSecurityQuestion(String username) {
        String sql = "SELECT security_question FROM users WHERE username = ?";
        try (PreparedStatement pstmt = dbConnection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("security_question");
            }
        } catch (SQLException e) {
            System.err.println("Error fetching security question: " + e.getMessage());
        }
        return null;
    }

    public boolean verifySecurityAnswer(String username, String answer) {
        String sql = "SELECT security_answer FROM users WHERE username = ?";
        try (PreparedStatement pstmt = dbConnection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String correctAnswer = rs.getString("security_answer");
                return correctAnswer != null && correctAnswer.equalsIgnoreCase(answer);
            }
        } catch (SQLException e) {
            System.err.println("Error verifying security answer: " + e.getMessage());
        }
        return false;
    }

    public boolean resetPassword(String username, String newPassword) {
        String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        String sql = "UPDATE users SET password_hash = ? WHERE username = ?";
        try (PreparedStatement pstmt = dbConnection.prepareStatement(sql)) {
            pstmt.setString(1, hashedPassword);
            pstmt.setString(2, username);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error resetting password: " + e.getMessage());
            return false;
        }
    }

    public boolean updateSecurityQuestion(String username, String newQuestion, String newAnswer) {
        String sql = "UPDATE users SET security_question = ?, security_answer = ? WHERE username = ?";
        try (PreparedStatement pstmt = dbConnection.prepareStatement(sql)) {
            pstmt.setString(1, newQuestion);
            pstmt.setString(2, newAnswer);
            pstmt.setString(3, username);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating security question: " + e.getMessage());
            return false;
        }
    }
}