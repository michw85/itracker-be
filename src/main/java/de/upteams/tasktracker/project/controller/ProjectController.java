package de.upteams.tasktracker.project.controller;

import de.upteams.tasktracker.project.controller.api.ProjectApi;
import de.upteams.tasktracker.project.dto.request.ProjectCreateDto;
import de.upteams.tasktracker.project.dto.response.ProjectResponseDto;
import de.upteams.tasktracker.project.dto.response.ProjectSummaryDto;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

//    public ProjectController(ProjectService service, ProjectMapper projectMapper) {
//        this.service = service;
//        this.projectMapper = projectMapper;
//    }

    @Override
    public ProjectResponseDto save(ProjectCreateDto newProjectDto, AuthUserDetails principal) {
        return service.save(newProjectDto, principal.user());
    }

    @Override
    public ProjectResponseDto getById(String id) {
        return service.getById(id);
    }

    @Override
    public List<ProjectResponseDto> getAll() {
        return service.getAll();
    }

    @Override
    public void deleteById(String id) {
        service.delete(id);
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
}
