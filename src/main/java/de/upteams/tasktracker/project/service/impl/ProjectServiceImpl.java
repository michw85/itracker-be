package de.upteams.tasktracker.project.service.impl;

import de.upteams.tasktracker.collaborator.entity.ProjectRoles;
import de.upteams.tasktracker.collaborator.persistence.CollaboratorRepository;
import de.upteams.tasktracker.collaborator.service.interfaces.CollaboratorService;
import de.upteams.tasktracker.project.dto.request.ProjectCreateDto;
import de.upteams.tasktracker.project.dto.response.ProjectResponseDto;
import de.upteams.tasktracker.project.dto.response.ProjectSummaryDto;
import de.upteams.tasktracker.project.entity.Project;
import de.upteams.tasktracker.project.exception.ProjectNotFoundException;
import de.upteams.tasktracker.project.persistence.ProjectRepository;
import de.upteams.tasktracker.project.service.interfaces.ProjectService;
import de.upteams.tasktracker.project.utils.ProjectMapper;
import de.upteams.tasktracker.task.entity.TaskStatus;
import de.upteams.tasktracker.task.persistence.TaskRepository;
import de.upteams.tasktracker.user.entity.AppUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for various operations with Projects
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository repository;
    private final ProjectMapper projectMapper;
    private final CollaboratorService collaboratorService;
    private final TaskRepository taskRepository;
    private final CollaboratorRepository collaboratorRepository;

    @Override
    public ProjectResponseDto save(ProjectCreateDto newProjectDto, AppUser projectOwner) {
        Project project = projectMapper.mapDtoToEntity(newProjectDto);
        project.setOwner(projectOwner);
        return projectMapper.toResponseDto(repository.save(project));
    }

    @Override
    public ProjectResponseDto getById(String id) {
        return projectMapper.toResponseDto(getOrTrow(id));
    }

    @Override
    public Project getOrTrow(String id) {
        return repository
                .findById(UUID.fromString(id))
                .orElseThrow(ProjectNotFoundException::new);
    }

    @Override
    public List<ProjectResponseDto> getAll() {
        return repository
                .findAll()
                .stream()
                .map(projectMapper::toResponseDto)
                .toList();
    }

    @Override
    public void delete(String id) {
        repository.deleteById(UUID.fromString(id));
    }

    @Override
    public String getUserRoleInProject(UUID projectId, UUID userId) {
        return collaboratorService.getUserRoleInProject(projectId, userId)
                .map(ProjectRoles::name)
                .orElseThrow(() -> new RuntimeException("User is not a member of this project"));
    }

    @Override
    public void checkProjectPermission(UUID projectId, UUID userId, List<String> allowedRoles) {
        String userRole = getUserRoleInProject(projectId, userId);
        if (!allowedRoles.contains(userRole)) {
            throw new RuntimeException("User does not have required permission");
        }
    }

    @Override
    public void checkProjectAccess(UUID projectId, UUID userId) {
        getUserRoleInProject(projectId, userId);
    }

    @Override
    public List<Project> getUserProjects(AppUser user) {
        log.info("Getting projects for user: {}", user.getEmail());

        List<Project> ownedProjects = repository.findByOwner(user);
        List<Project> memberProjects = collaboratorService.getProjectsByUser(user);

        Set<Project> allProjects = new HashSet<>();
        allProjects.addAll(ownedProjects);
        allProjects.addAll(memberProjects);

        return new ArrayList<>(allProjects);
    }

    // a method that implements an interface
    @Override
    public List<ProjectSummaryDto> getProjectSummaries(AppUser user) {
        log.info("Getting project summaries for user: {}", user.getEmail());

        List<Project> userProjects = getUserProjects(user);
        log.info("Found {} projects to summarize", userProjects.size());

        return userProjects.stream()
                .map(this::mapToSummary)
                .collect(Collectors.toList());
    }

    // private method for mapping
    private ProjectSummaryDto mapToSummary(Project project) {
        log.debug("Mapping project {} to summary", project.getId());

        // Counting active tasks
        int activeTasksCount = 0;
        try {
            activeTasksCount = taskRepository.countByProjectIdAndStatus(
                    project.getId(),
                    TaskStatus.ACTIVE
            );
            log.debug("Active tasks count: {}", activeTasksCount);
        } catch (Exception e) {
            log.warn("Error counting active tasks: {}", e.getMessage());
            activeTasksCount = taskRepository.countByProjectId(project.getId());
        }

        // Counting participants (collaborators + owner)
        int collaboratorsCount = 0;
        try {
            collaboratorsCount = collaboratorRepository.countByProjectId(project.getId());
            log.debug("Collaborators count: {}", collaboratorsCount);
        } catch (Exception e) {
            log.warn("Error counting collaborators: {}", e.getMessage());
        }
        int executorsCount = collaboratorsCount + 1; // +1 for owner

        return ProjectSummaryDto.builder()
                .id(project.getId())
                .title(project.getTitle())
                .description(project.getDescription())
                .activeTasksCount(activeTasksCount)
                .executorsCount(executorsCount)
                .status("OPEN")
                .build();
    }
}