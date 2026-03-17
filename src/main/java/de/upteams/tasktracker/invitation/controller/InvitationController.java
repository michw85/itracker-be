package de.upteams.tasktracker.invitation.controller;
import de.upteams.tasktracker.invitation.dto.request.InvitationRequestDto;
import de.upteams.tasktracker.invitation.dto.response.InvitationAcceptResponseDto;
import de.upteams.tasktracker.invitation.dto.response.InvitationResponseDto;
import de.upteams.tasktracker.invitation.service.InvitationService;
import de.upteams.tasktracker.security.service.AuthUserDetails;
import de.upteams.tasktracker.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing project invitations.
 * Provides endpoints for:
 *   Creating/updating invitations to projects
 *   Accepting invitations
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Invitation Controller", description = "Endpoints for managing project invitations")
public class InvitationController {

    // Dependencies are injected via constructor (thanks to @RequiredArgsConstructor)
    private final InvitationService invitationService;
    private final UserService userService;
    /**
     * Creates a new invitation or updates an existing one.
     *
     * @param projectId ID of the project to invite to
     * @param request   invitation details (email and role)
     * @param principal authenticated user creating the invitation
     * @return 200 OK for registered users, 202 Accepted for unregistered users
     */
    @PostMapping("/projects/{projectId}/invitations")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Invite a user to a project")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Invitation sent to registered user"),
            @ApiResponse(responseCode = "202", description = "Invitation sent to unregistered user"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "403", description = "No permission to invite"),
            @ApiResponse(responseCode = "404", description = "Project not found")
    })
    public ResponseEntity<InvitationResponseDto> createInvitation(
            @PathVariable String projectId,
            @RequestBody @Valid InvitationRequestDto request,
            @AuthenticationPrincipal AuthUserDetails principal
    ) {
        InvitationResponseDto response = invitationService.createOrUpdateInvitation(
                projectId, request, principal.user()
        );

        // Return different status codes based on whether user exists
        return userService.getByEmail(request.getEmail()).isPresent()
                ? ResponseEntity.ok(response)
                : ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    /**
     * Accepts an invitation using the provided token.
     * Only for already registered users.
     *
     * @param token     the invitation token
     * @param principal authenticated user accepting the invitation
     * @return acceptance confirmation
     */
    @PostMapping("/invitations/accept")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Accept a project invitation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully joined the project"),
            @ApiResponse(responseCode = "400", description = "Invalid or expired token"),
            @ApiResponse(responseCode = "403", description = "Email mismatch"),
            @ApiResponse(responseCode = "404", description = "Invitation not found")
    })
    public InvitationAcceptResponseDto acceptInvitation(
            @RequestParam String token,
            @AuthenticationPrincipal AuthUserDetails principal
    ) {
        return invitationService.acceptInvitation(token, principal.user());
    }
}
