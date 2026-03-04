package de.upteams.tasktracker.collaborator.entity;

import de.upteams.tasktracker.project.entity.Project;
import de.upteams.tasktracker.task.entity.Task;
import de.upteams.tasktracker.user.entity.AppUser;
import de.upteams.tasktracker.utils.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

import static de.upteams.tasktracker.utils.EntityUtil.getIdForToString;
import static de.upteams.tasktracker.utils.EntityUtil.getIdsForToString;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Collaborator extends BaseEntity {

    @NotNull
    @ManyToOne
    private AppUser appUser;

    @NotNull
    @ManyToOne
    private Project project;

    @NotNull
    @Enumerated(EnumType.STRING)
    private final Set<ProjectRoles> projectRolesSet = new HashSet<>();

    @ManyToMany
    private final Set<Task> tasks = new HashSet<>();

    @Override
    public String toString() {
        return "Collaborator{" +
                "id=" + id +
                ", tasks=" + getIdsForToString(tasks) +
                ", projectRolesSet=" + projectRolesSet +
                ", project=" + getIdForToString(project) +
                ", appUserId=" + getIdForToString(appUser) +
                '}';
    }
}
