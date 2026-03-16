package de.upteams.tasktracker.invitation.service;
import de.upteams.tasktracker.collaborator.entity.ProjectRoles;
import de.upteams.tasktracker.collaborator.service.interfaces.CollaboratorService;
import de.upteams.tasktracker.exception.handling.exceptions.invitation.InvitationException;
import de.upteams.tasktracker.invitation.dto.request.InvitationRequestDto;
import de.upteams.tasktracker.invitation.dto.response.InvitationAcceptResponseDto;
import de.upteams.tasktracker.invitation.dto.response.InvitationResponseDto;
import de.upteams.tasktracker.invitation.entity.Invitation;
import de.upteams.tasktracker.invitation.entity.InvitationStatus;
import de.upteams.tasktracker.invitation.mapper.InvitationMapper;
import de.upteams.tasktracker.invitation.repository.InvitationRepository;
import de.upteams.tasktracker.mail.EmailService;
import de.upteams.tasktracker.project.entity.Project;
import de.upteams.tasktracker.project.service.interfaces.ProjectService;
import de.upteams.tasktracker.user.entity.AppUser;
import de.upteams.tasktracker.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service responsible for managing project invitations.
 *
 * <p>Handles the complete invitation lifecycle:
 * <ul>
 *   <li>Creating new invitations for registered/unregistered users</li>
 *   <li>Resending invitations (updating expiration)</li>
 *   <li>Accepting invitations for registered users</li>
 *   <li>Processing pending invitations after user registration</li>
 * </ul>
 * </p>
 *
 * @see Invitation
 * @see InvitationRepository
 * @see EmailService
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InvitationService {

    private final InvitationRepository invitationRepository;
    private final ProjectService projectService;
    private final UserService userService;
    private final CollaboratorService collaboratorService;
    private final EmailService emailService;
    private final InvitationMapper invitationMapper;

    @Value("${app.base-url}")
    private String baseUrl;

    /**
     * Creates a new invitation or updates an existing pending invitation.
     * Business rules:
     *   Inviter must have OWNER or ADMIN role in the project
     *   User cannot be already a member of the project
     *   If pending invitation exists, refresh its expiration
     *   Send appropriate email based on user registration status
     *
     * @param projectId ID of the project to invite to
     * @param request   invitation details (email and role)
     * @param inviter   user creating the invitation
     * @return invitation response DTO
     * @throws InvitationException if validation fails or user lacks permissions
     */
    @Transactional
    public InvitationResponseDto createOrUpdateInvitation(
            String projectId,
            InvitationRequestDto request,
            AppUser inviter
    ) {
        log.info("Creating/updating invitation for project {} by user {}",
                projectId, inviter.getEmail());

        // Fetch and validate project exists
        Project project = projectService.getOrTrow(projectId);

        // Step 1: Verify inviter has permission to invite
        if (!canInvite(inviter, project)) {
            log.warn("User {} tried to invite without permission", inviter.getEmail());
            throw InvitationException.noPermission();
        }

        // Normalize email for consistent storage and lookup
        String email = request.getEmail().toLowerCase().trim();

        // Step 2: Check if user is already a project member
        Optional<AppUser> existingUser = userService.getByEmail(email);
        if (existingUser.isPresent() &&
                collaboratorService.isUserInProject(existingUser.get(), project)) {
            log.warn("User {} is already a member of project {}", email, project.getTitle());
            throw InvitationException.alreadyMember();
        }

        // Step 3: Look for existing pending invitation
        Optional<Invitation> existingInvitation = invitationRepository
                .findPendingByProjectAndEmail(project, email, InvitationStatus.PENDING);

        Invitation invitation;
        boolean isNewInvitation;

        if (existingInvitation.isPresent()) {
            // Update existing invitation - refresh expiration
            invitation = existingInvitation.get();
            invitation.refreshExpiration();
            isNewInvitation = false;
            log.info("Refreshed existing invitation for {} to project {}",
                    email, project.getTitle());
        } else {
            // Create new invitation
            invitation = new Invitation(project, email, request.getRole());
            isNewInvitation = true;
            log.info("Created new invitation for {} to project {}",
                    email, project.getTitle());
        }

        // Save invitation to database
        invitation = invitationRepository.save(invitation);

        // Step 4: Send appropriate email
        sendInvitationEmail(invitation, existingUser.isPresent());

        return invitationMapper.toResponseDto(invitation);
    }

    /**
     * Checks if a user has permission to invite others to a project.
     * Project owners and users with ADMIN role can invite.
     *
     * @param user    the user to check
     * @param project the project
     * @return true if user can invite, false otherwise
     */
    private boolean canInvite(AppUser user, Project project) {
        return project.getOwner().equals(user) ||
                collaboratorService.hasUserPermission(user, project,
                        List.of(ProjectRoles.OWNER, ProjectRoles.ADMIN));
    }

    /**
     * Sends an invitation email based on user registration status.
     *
     * @param invitation the invitation
     * @param userExists whether the invited user is already registered
     */
    private void sendInvitationEmail(Invitation invitation, boolean userExists) {
        String inviteLink = String.format("%s/invitation?token=%s",
                baseUrl, invitation.getInviteToken());

        if (userExists) {
            // Send invitation to registered user
            emailService.sendProjectInvitationEmail(
                    invitation.getEmail(),
                    invitation.getProject().getTitle(),
                    inviteLink
            );
            log.debug("Sent invitation email to registered user {}", invitation.getEmail());
        } else {
            // Send invitation with registration link to unregistered user
            emailService.sendRegistrationInvitationEmail(
                    invitation.getEmail(),
                    invitation.getProject().getTitle(),
                    inviteLink
            );
            log.debug("Sent registration invitation email to {}", invitation.getEmail());
        }
    }

    /**
     * Accepts an invitation for an already registered user.
     * Process:
     *   Validate invitation token exists and is valid
     *   Verify email matches the invited email
     *   Create Collaborator with specified role
     *   Mark invitation as USED
     *
     * @param token the invitation token
     * @param user  the authenticated user accepting the invitation
     * @return acceptance response DTO
     * @throws InvitationException if token invalid or email mismatch
     */
    @Transactional
    public InvitationAcceptResponseDto acceptInvitation(String token, AppUser user) {
        log.info("User {} accepting invitation with token {}", user.getEmail(), token);

        // Parse token and find invitation
        UUID tokenUuid;
        try {
            tokenUuid = UUID.fromString(token);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid token format: {}", token);
            throw InvitationException.invalidToken();
        }

        Invitation invitation = invitationRepository.findByInviteToken(tokenUuid)
                .orElseThrow(InvitationException::invalidToken);

        // Validate invitation is still valid
        if (!invitation.isValid()) {
            log.warn("Invitation {} is no longer valid", token);
            throw InvitationException.invalidToken();
        }

        // Verify email matches
        if (!invitation.getEmail().equalsIgnoreCase(user.getEmail())) {
            log.warn("Email mismatch for invitation {}: expected {}, got {}",
                    token, invitation.getEmail(), user.getEmail());
            throw new InvitationException(
                    HttpStatus.FORBIDDEN,
                    "This invitation was sent to a different email"
            );
        }

        // Create collaborator in the project
        collaboratorService.addCollaborator(
                invitation.getProject(),
                user,
                invitation.getRole()
        );

        // Mark invitation as used
        invitation.markAsUsed();
        invitationRepository.save(invitation);

        log.info("User {} successfully joined project {} via invitation",
                user.getEmail(), invitation.getProject().getTitle());

        return InvitationAcceptResponseDto.builder()
                .message("Successfully joined the project")
                .success(true)
                .projectId(invitation.getProject().getId().toString())
                .projectName(invitation.getProject().getTitle())
                .build();
    }

    /**
     * Processes all pending invitations for a user after they register.
     *This method is called automatically after successful registration.
     * It finds all valid invitations for the user's email and creates
     * Collaborator entries for each project.
     *
     * @param email   the email address used during registration
     * @param newUser the newly registered user
     */
    @Transactional
    public void processPendingInvitations(String email, AppUser newUser) {
        log.info("Processing pending invitations for email {}", email);

        List<Invitation> invitations = invitationRepository
                .findAllValidByEmail(email, InvitationStatus.PENDING, LocalDateTime.now());

        if (invitations.isEmpty()) {
            log.debug("No pending invitations found for {}", email);
            return;
        }

        for (Invitation invitation : invitations) {
            // Skip if user is already a member
            if (!collaboratorService.isUserInProject(newUser, invitation.getProject())) {
                collaboratorService.addCollaborator(
                        invitation.getProject(),
                        newUser,
                        invitation.getRole()
                );
                log.info("Added user {} to project {} via pending invitation",
                        email, invitation.getProject().getTitle());
            }

            // Mark invitation as used
            invitation.markAsUsed();
            invitationRepository.save(invitation);
        }
    }
}
