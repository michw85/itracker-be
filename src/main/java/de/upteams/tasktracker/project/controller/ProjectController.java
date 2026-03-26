package de.upteams.tasktracker.project.controller;

import de.upteams.tasktracker.collaborator.entity.ProjectRoles;
import de.upteams.tasktracker.collaborator.service.interfaces.CollaboratorService;
import de.upteams.tasktracker.project.controller.api.ProjectApi;
import de.upteams.tasktracker.project.dto.request.ProjectCreateDto;
import de.upteams.tasktracker.project.dto.response.ProjectResponseDto;
import de.upteams.tasktracker.project.dto.response.ProjectSummaryDto;
import de.upteams.tasktracker.project.entity.Project;
import de.upteams.tasktracker.project.service.interfaces.ProjectService;
import de.upteams.tasktracker.project.utils.ProjectMapper;
import de.upteams.tasktracker.security.service.AuthUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * REST Controller that receives http-requests for various operations with Projects
 */
@RestController
@RequestMapping("/api/v1/projects")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Projects", description = "Endpoints for managing projects")
public class ProjectController implements ProjectApi {

    private final ProjectService service;
    private final ProjectMapper projectMapper;
    private final CollaboratorService collaboratorService;

//    public ProjectController(ProjectService service, ProjectMapper projectMapper) {
//        this.service = service;
//        this.projectMapper = projectMapper;
//    }

    @PostMapping("/{id}/update")
    @PreAuthorize("isAuthenticated()")
    public ProjectResponseDto update(
            @PathVariable String id,
            @RequestBody ProjectCreateDto updateDto,
            @AuthenticationPrincipal AuthUserDetails principal
    ) {
        return service.update(id, updateDto, principal.user());
    }

    @GetMapping("/dashboard")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get project summaries for dashboard with statistics")
    public ResponseEntity<List<ProjectSummaryDto>> getProjectSummaries(
            @AuthenticationPrincipal AuthUserDetails principal
    ) {
        log.info("Getting project summaries for user: {}", principal.user().getEmail());

        List<ProjectSummaryDto> summaries = service.getProjectSummaries(principal.user());

        return ResponseEntity.ok(summaries);
    }

    /**
     * Get all projects for the current user
     */
    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get all projects for current user")
    public ResponseEntity<List<ProjectResponseDto>> getUserProjects(
            @AuthenticationPrincipal AuthUserDetails principal
    ) {
        log.info("Getting all projects for user: {}", principal.user().getEmail());

        List<ProjectResponseDto> projects = service.getUserProjects(principal.user())
                .stream()
                .map(projectMapper::toResponseDto)
                .toList();

        log.info("Found {} projects for user", projects.size());
        return ResponseEntity.ok(projects);
    }

    @GetMapping("/{id}/debug-permissions")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> debugPermissions(
            @PathVariable String id,
            @AuthenticationPrincipal AuthUserDetails principal
    ) {
        Map<String, Object> result = new HashMap<>();
        Project project = service.getOrTrow(id);

        result.put("projectId", id);
        result.put("projectTitle", project.getTitle());
        result.put("userId", principal.user().getId());
        result.put("userEmail", principal.user().getEmail());

        // Checking the role via collaboratorService
        Optional<ProjectRoles> role = collaboratorService.getUserRoleInProject(
                UUID.fromString(id), principal.user().getId());
        result.put("roleFromCollaborator", role.map(Enum::name).orElse("NONE"));

        // ПWe check whether the user is the owner
        boolean isOwner = project.getOwner() != null &&
                project.getOwner().getId().equals(principal.user().getId());
        result.put("isProjectOwner", isOwner);

        // Checking permissions using hasUserPermission
        boolean hasPermission = collaboratorService.hasUserPermission(
                principal.user(), project, List.of(ProjectRoles.OWNER, ProjectRoles.ADMIN));
        result.put("hasEditPermission", hasPermission);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    @Override
    public ProjectResponseDto getById(@PathVariable String id) {
        return service.getById(id);
    }

    @Override
    public ProjectResponseDto save(ProjectCreateDto newProjectDto, AuthUserDetails principal) {
        return service.save(newProjectDto, principal.user());
    }

    @Override
    public List<ProjectResponseDto> getAll() {
        return service.getAll();
    }

    @Override
    public void deleteById(String id) {
        service.delete(id);
    }
}
