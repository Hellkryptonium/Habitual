package com.habitual.ui.javafx;

public class UserSession {

    private static UserSession instance;

    private String username;
    private int userId;

    private UserSession() {}

    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public String getUsername() {
        return username;
    }

    public int getUserId() {
        return userId;
    }

    public void setLoggedInUser(String username, int userId) {
        this.username = username;
        this.userId = userId;
    }

    public void clearSession() {
        username = null;
        userId = 0;
        // Potentially reset instance to null or ensure it's cleared for next login
        // instance = null; // If you want a completely new session object next time
    }

    public boolean isLoggedIn() {
        return username != null && userId > 0;
    }
}
