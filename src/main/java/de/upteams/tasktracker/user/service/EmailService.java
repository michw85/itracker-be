package de.upteams.tasktracker.user.service;

public interface EmailService {
    void sendPasswordResetEmail(String userEmail, String token);
}
