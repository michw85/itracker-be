package de.upteams.tasktracker.project.controller;

import de.upteams.tasktracker.collaborator.entity.Collaborator;
import de.upteams.tasktracker.collaborator.entity.ProjectRoles;
import de.upteams.tasktracker.collaborator.persistence.CollaboratorRepository;
import de.upteams.tasktracker.collaborator.service.interfaces.CollaboratorService;
import de.upteams.tasktracker.invitation.entity.Invitation;
import de.upteams.tasktracker.invitation.repository.InvitationRepository;
import de.upteams.tasktracker.invitation.service.InvitationService;
import de.upteams.tasktracker.project.dto.MemberResponseDto;
import de.upteams.tasktracker.project.dto.ProjectRoleResponse;
import de.upteams.tasktracker.project.entity.Project;
import de.upteams.tasktracker.project.persistence.ProjectRepository;
import de.upteams.tasktracker.project.service.interfaces.ProjectService;
import de.upteams.tasktracker.security.service.AuthUserDetails;
import de.upteams.tasktracker.user.entity.AppUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Project Members", description = "Endpoints for managing project members")
public class ProjectMemberController {

    private final ProjectService projectService;
    private final InvitationService invitationService;
    private final CollaboratorService collaboratorService;
    private final InvitationRepository invitationRepository;
    private final CollaboratorRepository collaboratorRepository;
    private final ProjectRepository projectRepository;

    /**
     * 1. Get the current user's role in the project
     */
    @GetMapping("/{projectId}/my-role")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get current user's role in project")
    public ResponseEntity<ProjectRoleResponse> getMyRole(
            @PathVariable UUID projectId,
            @AuthenticationPrincipal AuthUserDetails principal
    ) {
        log.info("Getting role for user {} in project {}", principal.user().getEmail(), projectId);

        try {
            Optional<ProjectRoles> role = collaboratorService.getUserRoleInProject(
                    projectId, principal.user().getId()
            );

            if (role.isPresent()) {
                log.info("User role found: {}", role.get());
                return ResponseEntity.ok(new ProjectRoleResponse(role.get().name()));
            }

            // Let's check if the user is the owner of the project?
            Project project = projectService.getOrTrow(projectId.toString());
            if (project.getOwner() != null && project.getOwner().getId().equals(principal.user().getId())) {
                log.info("User is the OWNER of project {}", projectId);
                return ResponseEntity.ok(new ProjectRoleResponse(ProjectRoles.OWNER.name()));
            }

            log.warn("User {} is not a member of project {}", principal.user().getEmail(), projectId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        } catch (Exception e) {
            log.error("Error getting user role: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 2. Get project information
     */
    @GetMapping("/{projectId}/info")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get project information")
    public ResponseEntity<Project> getProjectInfo(
            @PathVariable UUID projectId,
            @AuthenticationPrincipal AuthUserDetails principal
    ) {
        log.info("Getting project info for {}", projectId);

        // Checking access
        Optional<ProjectRoles> role = collaboratorService.getUserRoleInProject(
                projectId, principal.user().getId()
        );

        if (role.isEmpty()) {
            // Let's check if the user is the owner of the project?
            Project project = projectService.getOrTrow(projectId.toString());
            if (project.getOwner() == null || !project.getOwner().getId().equals(principal.user().getId())) {
                log.warn("User {} does not have access to project {}",
                        principal.user().getEmail(), projectId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        Project project = projectService.getOrTrow(projectId.toString());
        return ResponseEntity.ok(project);
    }

    /**
     * 5. Get a list of project participants
     */
    @GetMapping("/{projectId}/members")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get project members")
    public ResponseEntity<List<MemberResponseDto>> getProjectMembers(
            @PathVariable UUID projectId,
            @AuthenticationPrincipal AuthUserDetails principal
    ) {
        log.info("Getting members for project {} by user {}",
                projectId, principal.user().getEmail());

        try {
            // Access check
            Optional<ProjectRoles> userRole = collaboratorService.getUserRoleInProject(
                    projectId, principal.user().getId()
            );

            // If the user is not a collaborator, we check whether he is the owner
            if (userRole.isEmpty()) {
                try {
                    Project project = projectService.getOrTrow(projectId.toString());
                    if (project.getOwner() != null &&
                            project.getOwner().getId().equals(principal.user().getId())) {
                        log.info("User is OWNER of project {}, granting access", projectId);
                        // The owner has access
                    } else {
                        log.warn("User {} is not a member of project {}",
                                principal.user().getEmail(), projectId);
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                    }
                } catch (Exception e) {
                    log.error("Project not found: {}", projectId);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
                }
            }

            // Getting Project Members
            List<MemberResponseDto> members = invitationService.getProjectMembers(projectId);
            return ResponseEntity.ok(members);

        } catch (Exception e) {
            log.error("Error getting project members: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Test endpoint for checking UUID conversion
     */
    @GetMapping("/uuid/{id}")
    public ResponseEntity<String> testUuid(@PathVariable String id) {
        log.info("Testing UUID conversion for: {}", id);

        try {
            UUID uuid = UUID.fromString(id);
            log.info("✅ Successfully converted to UUID: {}", uuid);

            String withHyphens = uuid.toString();
            String withoutHyphens = withHyphens.replace("-", "");

            StringBuilder response = new StringBuilder();
            response.append("✅ Valid UUID!\n");
            response.append("Original: ").append(id).append("\n");
            response.append("Parsed: ").append(uuid).append("\n");
            response.append("With hyphens: ").append(withHyphens).append("\n");
            response.append("Without hyphens: ").append(withoutHyphens).append("\n");
            response.append("Version: ").append(uuid.version()).append("\n");
            response.append("Variant: ").append(uuid.variant());

            return ResponseEntity.ok(response.toString());

        } catch (IllegalArgumentException e) {
            log.error("❌ Invalid UUID format: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    "❌ Invalid UUID format: " + id + "\nError: " + e.getMessage()
            );
        }
    }

    /**
     * A simple test to check the controller's operation
     */
    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("pong");
    }

    /**
     * TEMPORARY DEBUG ENDPOINT
     */
    @GetMapping("/debug/{projectId}/check")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> debugProjectCheck(
            @PathVariable UUID projectId,
            @AuthenticationPrincipal AuthUserDetails principal
    ) {
        log.info("========== DEBUG: Checking project {} ==========", projectId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("timestamp", new Date());
        result.put("projectId", projectId);
        result.put("userId", principal.user().getId());
        result.put("userEmail", principal.user().getEmail());

        // 1. Project verification
        Optional<Project> projectOpt = projectRepository.findById(projectId);
        result.put("projectExists", projectOpt.isPresent());

        if (projectOpt.isPresent()) {
            Project project = projectOpt.get();
            result.put("projectTitle", project.getTitle());
            result.put("projectOwner", project.getOwner() != null ? project.getOwner().getId() : null);
            result.put("isOwner", project.getOwner() != null &&
                    project.getOwner().getId().equals(principal.user().getId()));
        }

        // 2. Checking collaborators
        List<Collaborator> collaborators = collaboratorRepository.findByProjectId(projectId);
        result.put("collaboratorsCount", collaborators.size());

        List<Map<String, Object>> collabList = new ArrayList<>();
        for (Collaborator c : collaborators) {
            Map<String, Object> collabMap = new LinkedHashMap<>();
            collabMap.put("id", c.getId());
            collabMap.put("userId", c.getAppUser() != null ? c.getAppUser().getId() : null);
            collabMap.put("userEmail", c.getAppUser() != null ? c.getAppUser().getEmail() : null);
            collabMap.put("roles", c.getProjectRolesSet());
            collabList.add(collabMap);
        }
        result.put("collaborators", collabList);

        // 3. Checking invitations
        List<Invitation> invitations = invitationRepository.findByProjectId(projectId);
        result.put("invitationsCount", invitations.size());

        List<Map<String, Object>> invList = new ArrayList<>();
        for (Invitation inv : invitations) {
            Map<String, Object> invMap = new LinkedHashMap<>();
            invMap.put("id", inv.getId());
            invMap.put("email", inv.getEmail());
            invMap.put("role", inv.getRole());
            invMap.put("status", inv.getStatus());
            invMap.put("userId", inv.getUser() != null ? inv.getUser().getId() : null);
            invMap.put("invitedAt", inv.getInvitedAt());
            invList.add(invMap);
        }
        result.put("invitations", invList);

        // 4. Checking the current user's role
        var userRole = collaboratorService.getUserRoleInProject(projectId, principal.user().getId());
        result.put("currentUserRole", userRole.map(Enum::name).orElse("NONE"));

        // 5. Calling the service to get members
        try {
            log.info("Calling invitationService.getProjectMembers({})", projectId);
            List<MemberResponseDto> members = invitationService.getProjectMembers(projectId);
            result.put("serviceMembersCount", members.size());
            result.put("serviceMembers", members.stream()
                    .map(m -> Map.of(
                            "email", m.getEmail(),
                            "role", m.getRole(),
                            "status", m.getStatus()
                    ))
                    .collect(Collectors.toList()));
        } catch (Exception e) {
            log.error("Error in debug endpoint: {}", e.getMessage(), e);
            result.put("error", e.getMessage());
            result.put("errorType", e.getClass().getName());
        }

        log.info("========== DEBUG COMPLETE ==========");
        return ResponseEntity.ok(result);
    }
}