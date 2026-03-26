package de.upteams.tasktracker.project.service.interfaces;

import de.upteams.tasktracker.project.dto.request.ProjectCreateDto;
import de.upteams.tasktracker.project.dto.response.ProjectResponseDto;
import de.upteams.tasktracker.project.dto.response.ProjectSummaryDto;
import de.upteams.tasktracker.project.entity.Project;
import de.upteams.tasktracker.user.entity.AppUser;

import java.util.List;
import java.util.UUID;

/**
 * Service for various operations with Projects
 */
public interface ProjectService {

    ProjectResponseDto save(ProjectCreateDto newProjectDto, AppUser projectOwner);

    ProjectResponseDto getById(String id);

    Project getOrTrow(String id);

    List<ProjectResponseDto> getAll();

    void delete(String id);

    String getUserRoleInProject(UUID projectId, UUID userId);

    void checkProjectPermission(UUID projectId, UUID userId, List<String> allowedRoles);

    void checkProjectAccess(UUID projectId, UUID userId);

    List<Project> getUserProjects(AppUser user);

    List<ProjectSummaryDto> getProjectSummaries(AppUser user);

    ProjectResponseDto update(String id, ProjectCreateDto updateDto, AppUser user);
}
