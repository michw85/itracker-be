package de.upteams.tasktracker.user.controller.impl;

import de.upteams.tasktracker.user.controller.interfaces.PasswordController;
import de.upteams.tasktracker.user.dto.request.ForgotPasswordRequest;
import de.upteams.tasktracker.user.dto.request.ResetPasswordRequestDto;
import de.upteams.tasktracker.user.service.UserService;
import de.upteams.tasktracker.user.service.EmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class PasswordControllerImpl implements PasswordController {

    private String baseUrl = "http://localhost:5173";
    private String resetPath = "/#/reset-password?token=";

    private final UserService userService;
    private final EmailService emailService;

    @Override

    public ResponseEntity<String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        String token = userService.createPasswordResetToken(request.getEmail());


        emailService.sendPasswordResetEmail(request.getEmail(), token);
        return ResponseEntity.ok("Reset password process initiated for: " + request.getEmail());
    }

    @Override
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordRequestDto request) {
        userService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok("Password has been successfully reset!");
    }
}