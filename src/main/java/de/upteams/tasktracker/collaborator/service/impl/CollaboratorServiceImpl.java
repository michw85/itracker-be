package de.upteams.tasktracker.collaborator.service.impl;

import de.upteams.tasktracker.collaborator.entity.Collaborator;
import de.upteams.tasktracker.collaborator.entity.ProjectRoles;
import de.upteams.tasktracker.collaborator.persistence.CollaboratorRepository;
import de.upteams.tasktracker.collaborator.service.interfaces.CollaboratorService;
import de.upteams.tasktracker.project.entity.Project;
import de.upteams.tasktracker.user.entity.AppUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CollaboratorServiceImpl implements CollaboratorService {

    private final CollaboratorRepository collaboratorRepository;

    @Override
    public boolean isUserInProject(AppUser user, Project project) {
        return getCollaborator(user, project).isPresent();
    }

    @Override
    public Optional<Collaborator> getCollaborator(AppUser user, Project project) {
        return collaboratorRepository.findCollaborator(user, project);
    }

    @Override
    public boolean hasUserPermission(AppUser user, Project project, ProjectRoles requiredRole) {
        return hasUserPermission(user, project, Collections.singletonList(requiredRole));
    }

    @Override
    public boolean hasUserPermission(AppUser user, Project project, Collection<ProjectRoles> requiredRoles) {
        return getCollaborator(user, project)
                .map(collaborator -> hasAnyRequiredRole(collaborator, requiredRoles))
                .orElse(false);
    }

    private boolean hasAnyRequiredRole(Collaborator collaborator, Collection<ProjectRoles> requiredRoles) {
        return collaborator.getProjectRolesSet()
                .stream()
                .anyMatch(requiredRoles::contains);
    }

    @Override
    public Collaborator addCollaborator(Project project, AppUser user, List<ProjectRoles> roles) {
        Collaborator collaborator = new Collaborator();
        collaborator.setProject(project);
        collaborator.setAppUser(user);

        // Adding several roles
        collaborator.getProjectRolesSet().addAll(roles);

        return collaboratorRepository.save(collaborator);
    }

    @Override
    public Collaborator addCollaborator(Project project, AppUser user, ProjectRoles role) {
        // Checking if the user is already a collaborator
        Optional<Collaborator> existing = getCollaborator(user, project);
        if (existing.isPresent()) {
            return existing.get();
        }

        // We are creating a new collaborator
        Collaborator collaborator = new Collaborator();
        collaborator.setAppUser(user);
        collaborator.setProject(project);
        collaborator.getProjectRolesSet().add(role);

        return collaboratorRepository.save(collaborator);
    }

    @Override
    public Optional<ProjectRoles> getUserRoleInProject(UUID projectId, UUID userId) {
        log.info("Getting role for user {} in project {}", userId, projectId);

        Optional<Collaborator> collaboratorOpt = collaboratorRepository
                .findByProjectIdAndUserId(projectId, userId);

        if (collaboratorOpt.isEmpty()) {
            log.warn("No collaborator found for user {} in project {}", userId, projectId);
            return Optional.empty();
        }

        Collaborator collaborator = collaboratorOpt.get();
        Set<ProjectRoles> roles = collaborator.getProjectRolesSet();
        log.info("User has roles: {}", roles);

        // Returning the first role by priority
        if (roles.contains(ProjectRoles.OWNER)) {
            return Optional.of(ProjectRoles.OWNER);
        } else if (roles.contains(ProjectRoles.ADMIN)) {
            return Optional.of(ProjectRoles.ADMIN);
        } else if (roles.contains(ProjectRoles.MEMBER)) {
            return Optional.of(ProjectRoles.MEMBER);
        } else if (roles.contains(ProjectRoles.VIEWER)) {
            return Optional.of(ProjectRoles.VIEWER);
        }

        return Optional.empty();
    }

    @Override
    public List<Project> getProjectsByUser(AppUser user) {
        return collaboratorRepository.findByAppUser(user)
                .stream()
                .map(Collaborator::getProject)
                .collect(Collectors.toList());
    }

    @Override
    public List<Project> getProjectsByUserId(UUID userId) {
        return collaboratorRepository.findByAppUserId(userId)
                .stream()
                .map(Collaborator::getProject)
                .collect(Collectors.toList());
    }

}
