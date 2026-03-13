package de.upteams.tasktracker.user.controller.interfaces;

import de.upteams.tasktracker.user.dto.request.ForgotPasswordRequest;
import de.upteams.tasktracker.user.dto.request.ResetPasswordRequestDto;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public interface PasswordController {
    @PostMapping("/forgot-password")
    ResponseEntity<String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request);

@PostMapping("/reset-password")
 ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequestDto request);

}



