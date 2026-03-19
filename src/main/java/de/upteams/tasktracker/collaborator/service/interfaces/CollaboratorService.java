package de.upteams.tasktracker.collaborator.service.interfaces;

import de.upteams.tasktracker.collaborator.entity.Collaborator;
import de.upteams.tasktracker.collaborator.entity.ProjectRoles;
import de.upteams.tasktracker.project.entity.Project;
import de.upteams.tasktracker.user.entity.AppUser;
import org.w3c.dom.stylesheets.LinkStyle;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CollaboratorService {

    boolean isUserInProject(AppUser user, Project project);

    Optional<Collaborator> getCollaborator(AppUser user, Project project);

    boolean hasUserPermission(AppUser user, Project project, ProjectRoles requiredRole);

    boolean hasUserPermission(AppUser user, Project project, Collection<ProjectRoles> requiredRoles);

    Collaborator addCollaborator(Project project, AppUser user, ProjectRoles role);

    // New method for multiple roles (with default implementation)
    default Collaborator addCollaborator(Project project, AppUser user, List<ProjectRoles> roles) {
        Collaborator collaborator = addCollaborator(project, user, roles.get(0));
        // Add the remaining roles
        for (int i = 1; i < roles.size(); i++) {
            collaborator.getProjectRolesSet().add(roles.get(i));
        }
        return collaborator;
    }

    /**
     * Get a user role in a project
     */
    Optional<ProjectRoles> getUserRoleInProject(UUID projectId, UUID userId);

    /**
     * Check if the user has one of the allowed roles
     */
    default boolean hasAnyRole(UUID projectId, UUID userId, List<ProjectRoles> allowedRoles) {
        return getUserRoleInProject(projectId, userId)
                .map(allowedRoles::contains)
                .orElse(false);
    }

    List<Project> getProjectsByUser(AppUser user);

    List<Project> getProjectsByUserId(UUID userId);
}
