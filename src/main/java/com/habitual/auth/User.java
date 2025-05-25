package com.habitual.auth;

public class User {
    private int id;
    private String username;
    private String passwordHash;
    private String securityQuestion;
    private String securityAnswer;

    public User(int id, String username, String passwordHash, String securityQuestion, String securityAnswer) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.securityQuestion = securityQuestion;
        this.securityAnswer = securityAnswer;
    }

    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public String getSecurityQuestion() { return securityQuestion; }
    public String getSecurityAnswer() { return securityAnswer; }

    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public void setSecurityQuestion(String securityQuestion) { this.securityQuestion = securityQuestion; }
    public void setSecurityAnswer(String securityAnswer) { this.securityAnswer = securityAnswer; }
}
