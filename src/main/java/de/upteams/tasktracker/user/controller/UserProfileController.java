package de.upteams.tasktracker.user.controller;

import de.upteams.tasktracker.security.service.AuthUserDetails;
import de.upteams.tasktracker.user.dto.request.PasswordChangeDto;
import de.upteams.tasktracker.user.dto.request.ProfileUpdateDto;
import de.upteams.tasktracker.user.dto.response.UserResponseDto;
import de.upteams.tasktracker.user.service.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/users/profile")
@RequiredArgsConstructor
@Tag(name = "User Profile", description = "Endpoints for managing user profiles")
public class UserProfileController {

    private final UserProfileService profileService;

    @Operation(summary = "Get current user profile")
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public UserResponseDto getCurrentUserProfile(
            @AuthenticationPrincipal AuthUserDetails principal
    ) {
        return profileService.getUserProfile(principal.user().getId().toString());
    }

    @Operation(summary = "Get user profile by ID")
    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponseDto getUserProfile(
            @PathVariable String userId
    ) {
        return profileService.getUserProfile(userId);
    }

    @Operation(summary = "Update current user profile")
    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public UserResponseDto updateProfile(
            @AuthenticationPrincipal AuthUserDetails principal,
            @RequestBody @Valid ProfileUpdateDto updateDto
    ) {
        return profileService.updateProfile(principal.user().getId().toString(), updateDto);
    }


    @Operation(summary = "Upload avatar")
    @PostMapping("/me/avatar")
    @PreAuthorize("isAuthenticated()")
    public UserResponseDto uploadAvatar(
            @AuthenticationPrincipal AuthUserDetails principal,
            @RequestParam("avatar") MultipartFile file
    ) {
        return profileService.uploadAvatar(principal.user().getId().toString(), file);
    }

    @Operation(summary = "Delete avatar")
    @DeleteMapping("/me/avatar")
    @PreAuthorize("isAuthenticated()")
    public UserResponseDto deleteAvatar(
            @AuthenticationPrincipal AuthUserDetails principal
    ) {
        return profileService.deleteAvatar(principal.user().getId().toString());
    }

    @Operation(summary = "Change password")
    @PutMapping("/me/password")
    public ResponseEntity<String> changePassword(
            @AuthenticationPrincipal AuthUserDetails principal,
            @RequestBody @Valid PasswordChangeDto dto
    ) {
        profileService.changePassword(principal.user().getId().toString(), dto);
        return ResponseEntity.ok("Password successfully changed");
    }
}