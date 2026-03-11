package de.upteams.tasktracker.user.controller.impl;

import de.upteams.tasktracker.user.controller.interfaces.PasswordController;
import de.upteams.tasktracker.user.dto.request.ForgotPasswordRequest;
import de.upteams.tasktracker.user.dto.request.ResetPasswordRequestDto;
import de.upteams.tasktracker.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class PasswordControllerImpl implements PasswordController {

    private final UserService userService;

    @Override

    public ResponseEntity<String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        String token = userService.createPasswordResetToken(request.getEmail());
        return ResponseEntity.ok("Reset password process initiated for: " + request.getEmail());
    }

    @Override
    public ResponseEntity<String> resetPassword(ResetPasswordRequestDto request) {
        userService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok("Password has been successfully reset!");
    }
}