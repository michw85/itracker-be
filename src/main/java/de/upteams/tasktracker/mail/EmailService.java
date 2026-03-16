package de.upteams.tasktracker.mail;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Service for email sending
 */
@Service
@RequiredArgsConstructor
public class EmailService {

    @Value("${app.base-url}")
    private String baseUrl;

    private final EmailSender emailSender;
    private final TemplateEngine templateEngine;

    @Async
    public void sendConfirmationEmail(String sentTo, String confirmationCode) {
        String confirmationLink = "%s/api/v1/users/confirm/%s".formatted(baseUrl, confirmationCode);

        Map<String, Object> model = Map.of(
                "link", confirmationLink
        );

        String htmlContent = templateEngine.generateHtml("confirm_registration_mail.ftlh", model);
        emailSender.sendEmail(sentTo, "Confirm your registration", htmlContent);
    }

    public void sendProjectInvitationEmail(String to, String projectName, String inviteLink) {
        String subject = String.format("Invitation to the project %s", projectName);
        Map<String, Object> model = Map.of(
                "projectName", projectName,
                "inviteLink", inviteLink
        );
        String html = templateEngine.generateHtml("project_invitation.ftlh", model);
        emailSender.sendEmail(to, subject, html);
    }

    public void sendRegistrationInvitationEmail(String to, String projectName, String inviteLink) {
        String subject = String.format("Invitation to register and join the project %s", projectName);
        Map<String, Object> model = Map.of(
                "projectName", projectName,
                "registrationLink", inviteLink
        );
        String html = templateEngine.generateHtml("registration_invitation.ftlh", model);
        emailSender.sendEmail(to, subject, html);
    }
}
