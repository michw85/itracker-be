package de.upteams.tasktracker.user.service.impl;

import de.upteams.tasktracker.user.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    private String baseUrl = "http://localhost:5173";

    @Override
    public void sendPasswordResetEmail(String userEmail, String token) {

        String resetUrl = baseUrl + "/#/reset-password?token=" + token;

        String content = "<p>Hello,</p>" +
                "<p>You have requested to reset your password for your TaskTracker account.</p>" +
                "<p>Please click the link below to set a new password:</p>" +
                "<p><a href=\"" + resetUrl + "\">Reset Password</a></p>" +
                "<p>This link will expire in 15 minutes.</p>" +
                "<p>If you did not request this, please ignore this email.</p>";

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(userEmail);
            helper.setSubject("TaskTracker - Password Reset Request");
            helper.setText(content, true);

            mailSender.send(message);
            log.info("Reset email successfully sent to: {}", userEmail);
        } catch (MessagingException e) {
            log.error("Failed to send password reset email to {}: {}", userEmail, e.getMessage());
            throw new RuntimeException("Email delivery failed");
        }

    }
}
