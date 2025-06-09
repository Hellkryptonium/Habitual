package com.habitual.utils;

import javafx.scene.control.Control;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.DatePicker;

/**
 * Utility class for input validation across the application.
 * Provides reusable validation methods for text fields, date pickers, and other form controls.
 */
public class ValidationUtils {
    
    /**
     * Validates that a text field is not empty
     * 
     * @param field The TextField to validate
     * @param fieldName Human-readable name of the field for error message
     * @return Error message if invalid, null if valid
     */
    public static String validateNotEmpty(TextField field, String fieldName) {
        if (field.getText() == null || field.getText().trim().isEmpty()) {
            field.requestFocus();
            return fieldName + " cannot be empty.";
        }
        return null;
    }
    
    /**
     * Validates that a password field is not empty
     * 
     * @param field The PasswordField to validate
     * @param fieldName Human-readable name of the field for error message
     * @return Error message if invalid, null if valid
     */
    public static String validateNotEmpty(PasswordField field, String fieldName) {
        if (field.getText() == null || field.getText().isEmpty()) {
            field.requestFocus();
            return fieldName + " cannot be empty.";
        }
        return null;
    }
    
    /**
     * Validates that a DatePicker has a selected date
     * 
     * @param datePicker The DatePicker to validate
     * @param fieldName Human-readable name of the field for error message
     * @return Error message if invalid, null if valid
     */
    public static String validateDateSelected(DatePicker datePicker, String fieldName) {
        if (datePicker.getValue() == null) {
            datePicker.requestFocus();
            return "Please select a " + fieldName + ".";
        }
        return null;
    }
    
    /**
     * Validates that a string is within a certain length
     * 
     * @param text The text to validate
     * @param minLength Minimum allowed length
     * @param maxLength Maximum allowed length
     * @param fieldName Human-readable name of the field for error message
     * @return Error message if invalid, null if valid
     */
    public static String validateLength(String text, int minLength, int maxLength, String fieldName) {
        if (text == null) {
            return fieldName + " cannot be null.";
        }
        
        int length = text.trim().length();
        if (length < minLength) {
            return fieldName + " must be at least " + minLength + " characters.";
        }
        
        if (length > maxLength) {
            return fieldName + " cannot exceed " + maxLength + " characters.";
        }
        
        return null;
    }
    
    /**
     * Validates that a string contains no potentially dangerous characters for SQL
     * 
     * @param text The text to validate
     * @param fieldName Human-readable name of the field for error message
     * @return Error message if invalid, null if valid
     */
    public static String validateNoSQLInjection(String text, String fieldName) {
        if (text == null) {
            return null; // Empty values should be handled by validateNotEmpty
        }
        
        if (text.contains("'") || text.contains("\"") || text.contains(";") || 
            text.contains("--") || text.contains("/*") || text.contains("*/")) {
            return fieldName + " contains invalid characters.";
        }
        
        return null;
    }
    
    /**
     * Applies high-contrast style to a control to indicate validation error
     * 
     * @param control The control to style
     * @param isError True to apply error style, false to remove it
     */
    public static void setErrorStyle(Control control, boolean isError) {
        if (isError) {
            control.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
        } else {
            control.setStyle("");
        }
    }
    
    /**
     * Validates a password for minimum security requirements
     * 
     * @param password The password to validate
     * @return Error message if invalid, null if valid
     */
    public static String validatePassword(String password) {
        if (password == null || password.length() < 8) {
            return "Password must be at least 8 characters long.";
        }
        
        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            if (Character.isLowerCase(c)) hasLower = true;
            if (Character.isDigit(c)) hasDigit = true;
        }
        
        if (!hasUpper || !hasLower || !hasDigit) {
            return "Password must contain uppercase letters, lowercase letters, and numbers.";
        }
        
        return null;
    }
}
