package de.upteams.tasktracker.invitation.service;

import de.upteams.tasktracker.collaborator.entity.Collaborator;
import de.upteams.tasktracker.collaborator.entity.ProjectRoles;
import de.upteams.tasktracker.collaborator.persistence.CollaboratorRepository;
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
import de.upteams.tasktracker.project.dto.MemberResponseDto;
import de.upteams.tasktracker.project.entity.Project;
import de.upteams.tasktracker.project.persistence.ProjectRepository;
import de.upteams.tasktracker.project.service.interfaces.ProjectService;
import de.upteams.tasktracker.security.service.AuthUserDetails;
import de.upteams.tasktracker.user.entity.AppUser;
import de.upteams.tasktracker.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Service responsible for managing project invitations.
 *
 * Handles the complete invitation lifecycle:
 *   Creating new invitations for registered/unregistered users
 *   Resending invitations (updating expiration)
 *   Accepting invitations for registered users
 *   Processing pending invitations after user registration
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
    private final ProjectRepository projectRepository;
    private final UserService userService;
    private final CollaboratorService collaboratorService;
    private final EmailService emailService;
    private final InvitationMapper invitationMapper;
    private final CollaboratorRepository collaboratorRepository;

    @Value("${app.base-url}")
    private String baseUrl;

    /**
     * Creates a new invitation or updates an existing pending invitation.
     * Business rules:
     * Inviter must have OWNER or ADMIN role in the project
     * User cannot be already a member of the project
     * If pending invitation exists, refresh its expiration
     * Send appropriate email based on user registration status
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
     * Validate invitation token exists and is valid
     * Verify email matches the invited email
     * Create Collaborator with specified role
     * Mark invitation as USED
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
     * This method is called automatically after successful registration.
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

    /**
     * Get a list of all project participants (including pending invitations)
     */
//    @Transactional(readOnly = true)
//    public List<MemberResponseDto> getProjectMembers(UUID projectId) {
//        log.info("========== INVITATION SERVICE: getProjectMembers ==========");
//        log.info("Project ID: {}", projectId);
//
//        try {
//            // Check: Does the project exist?
//            boolean projectExists = projectRepository.existsById(projectId);
//            log.info("Project exists in DB: {}", projectExists);
//
//            if (!projectExists) {
//                log.warn("Project {} does not exist", projectId);
//                return new ArrayList<>();
//            }
//
//            // Search for invitations
//            log.info("Searching invitations in repository...");
//            List<Invitation> invitations = invitationRepository.findByProjectId(projectId);
//            log.info("Repository returned {} invitations", invitations.size());
//
//            List<MemberResponseDto> result = new ArrayList<>();
//
//            if (!invitations.isEmpty()) {
//                // Invitation mapping
//                for (int i = 0; i < invitations.size(); i++) {
//                    Invitation inv = invitations.get(i);
//                    log.info("Processing invitation {}: id={}, email={}, status={}",
//                            i, inv.getId(), inv.getEmail(), inv.getStatus());
//
//                    try {
//                        MemberResponseDto dto = mapToMemberResponse(inv);
//                        result.add(dto);
//                        log.info("Successfully mapped invitation {}", i);
//                    } catch (Exception e) {
//                        log.error("Error mapping invitation {}: {}", i, e.getMessage(), e);
//                    }
//                }
//            }
//
//            // Add participants from collaborator (if they are not included in the invitations)
//            List<MemberResponseDto> collaboratorMembers = getMembersFromCollaborators(projectId);
//
//            // Consolidate and avoid duplicate emails
//            Set<String> existingEmails = result.stream()
//                    .map(MemberResponseDto::getEmail)
//                    .collect(Collectors.toSet());
//
//            for (MemberResponseDto collabMember : collaboratorMembers) {
//                if (!existingEmails.contains(collabMember.getEmail())) {
//                    result.add(collabMember);
//                }
//            }
//
//            log.info("Returning {} members total", result.size());
//            return result;
//
//        } catch (Exception e) {
//            log.error("!!! EXCEPTION in getProjectMembers: {}", e.getMessage(), e);
//            throw new RuntimeException("Failed to get project members", e);
//        }
//    }

    @Transactional(readOnly = true)
    public List<MemberResponseDto> getProjectMembers(UUID projectId) {
        log.info("========== INVITATION SERVICE: getProjectMembers ==========");
        log.info("Project ID: {}", projectId);

        List<MemberResponseDto> result = new ArrayList<>();

        try {
            // 1. Получаем приглашения
            log.info("Searching invitations in repository...");
            List<Invitation> invitations = invitationRepository.findByProjectId(projectId);
            log.info("Repository returned {} invitations", invitations.size());

            // Маппинг приглашений
            for (Invitation inv : invitations) {
                try {
                    MemberResponseDto dto = mapToMemberResponse(inv);
                    result.add(dto);
                    log.debug("Mapped invitation: {}", inv.getEmail());
                } catch (Exception e) {
                    log.error("Error mapping invitation: {}", e.getMessage());
                }
            }

            // 2. Добавляем участников из collaborator (если их нет в приглашениях)
            List<MemberResponseDto> collaboratorMembers = getMembersFromCollaborators(projectId);

            // Объединяем, избегая дубликатов по email
            Set<String> existingEmails = result.stream()
                    .map(MemberResponseDto::getEmail)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            for (MemberResponseDto collabMember : collaboratorMembers) {
                if (collabMember.getEmail() != null && !existingEmails.contains(collabMember.getEmail())) {
                    result.add(collabMember);
                    log.debug("Added collaborator: {}", collabMember.getEmail());
                }
            }

            // 3. Добавляем владельца проекта, если его нет в списке
            try {
                Project project = projectService.getOrTrow(projectId.toString());
                if (project.getOwner() != null) {
                    String ownerEmail = project.getOwner().getEmail();
                    if (!existingEmails.contains(ownerEmail)) {
                        MemberResponseDto ownerDto = MemberResponseDto.builder()
                                .id(project.getOwner().getId())
                                .email(ownerEmail)
                                .role(ProjectRoles.OWNER)
                                .status("ACTIVE")
                                .userId(project.getOwner().getId())
                                .build();
                        result.add(0, ownerDto); // Добавляем в начало списка
                        log.debug("Added project owner: {}", ownerEmail);
                    }
                }
            } catch (Exception e) {
                log.warn("Could not add project owner: {}", e.getMessage());
            }

            log.info("Returning {} members total", result.size());
            return result;

        } catch (Exception e) {
            log.error("!!! EXCEPTION in getProjectMembers: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get project members", e);
        }
    }

    @GetMapping("/debug/project/{projectId}/full-info")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> debugFullInfo(
            @PathVariable UUID projectId,
            @AuthenticationPrincipal AuthUserDetails principal
    ) {
        Map<String, Object> debug = new HashMap<>();

        // Информация о пользователе
        debug.put("currentUser", Map.of(
                "id", principal.user().getId(),
                "email", principal.user().getEmail()
        ));

        // Информация о проекте
        Project project = projectService.getOrTrow(projectId.toString());
        debug.put("project", Map.of(
                "id", project.getId(),
                "title", project.getTitle(),
                "ownerId", project.getOwner() != null ? project.getOwner().getId() : null,
                "ownerEmail", project.getOwner() != null ? project.getOwner().getEmail() : null,
                "isCurrentUserOwner", project.getOwner() != null &&
                        project.getOwner().getId().equals(principal.user().getId())
        ));

        // Коллабораторы
        List<Collaborator> collaborators = collaboratorRepository.findByProjectId(projectId);
        debug.put("collaborators", collaborators.stream()
                .map(c -> Map.of(
                        "id", c.getId(),
                        "userId", c.getAppUser() != null ? c.getAppUser().getId() : null,
                        "userEmail", c.getAppUser() != null ? c.getAppUser().getEmail() : null,
                        "roles", c.getProjectRolesSet()
                ))
                .toList());

        // Приглашения
        List<Invitation> invitations = invitationRepository.findByProjectId(projectId);
        debug.put("invitations", invitations.stream()
                .map(i -> Map.of(
                        "id", i.getId(),
                        "email", i.getEmail(),
                        "role", i.getRole(),
                        "status", i.getStatus()
                ))
                .toList());

        return ResponseEntity.ok(debug);
    }

    private List<MemberResponseDto> getMembersFromCollaborators(UUID projectId) {
        log.info("Getting members from collaborators for project {}", projectId);

        List<Collaborator> collaborators = collaboratorRepository.findByProjectId(projectId);

        if (collaborators.isEmpty()) {
            log.info("No collaborators found for project {}", projectId);
            return new ArrayList<>();
        }

        List<MemberResponseDto> result = new ArrayList<>();
        for (Collaborator collab : collaborators) {
            try {
                // We get the first role from Set
                ProjectRoles role = collab.getProjectRolesSet().stream()
                        .findFirst()
                        .orElse(null);

                if (role == null) {
                    log.warn("Collaborator {} has no roles", collab.getId());
                    continue;
                }

                MemberResponseDto dto = MemberResponseDto.builder()
                        .id(collab.getId())
                        .email(collab.getAppUser() != null ? collab.getAppUser().getEmail() : null)
                        .role(role)
                        .status("ACTIVE")
                        .invitedAt(null) // or LocalDateTime.now() if needed
                        .expiresAt(null)
                        .userId(collab.getAppUser() != null ? collab.getAppUser().getId() : null)
                        .build();

                result.add(dto);
                log.debug("Added member from collaborator: {}", dto.getEmail());

            } catch (Exception e) {
                log.error("Error mapping collaborator {}: {}", collab.getId(), e.getMessage());
            }
        }

        log.info("Returning {} members from collaborators", result.size());
        return result;
    }

    /**
     * Get only accepted project participants
     */
    @Transactional(readOnly = true)
    public List<MemberResponseDto> getAcceptedMembers(UUID projectId) {
        log.info("Getting accepted members for project {}", projectId);

        List<Invitation> accepted = invitationRepository.findAcceptedByProjectId(projectId);

        return accepted.stream()
                .map(this::mapToMemberResponse)
                .collect(Collectors.toList());
    }

    private MemberResponseDto mapToMemberResponse(Invitation invitation) {
        log.debug("Mapping invitation: {}", invitation.getId());

        UUID userId = null;
        if (invitation.getUser() != null) {
            userId = invitation.getUser().getId();
            log.debug("Invitation has user with ID: {}", userId);
        }

        MemberResponseDto dto = MemberResponseDto.builder()
                .id(invitation.getId())
                .email(invitation.getEmail())
                .role(invitation.getRole())
                .status(invitation.getStatus().name())
                .invitedAt(invitation.getInvitedAt())
                .expiresAt(invitation.getExpiresAt())
                .userId(userId)
                .build();

        log.debug("Mapped DTO: id={}, email={}, role={}",
                dto.getId(), dto.getEmail(), dto.getRole());

        return dto;
    }
}
