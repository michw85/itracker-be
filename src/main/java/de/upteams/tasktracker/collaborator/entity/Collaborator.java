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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static de.upteams.tasktracker.utils.EntityUtil.getIdForToString;
import static de.upteams.tasktracker.utils.EntityUtil.getIdsForToString;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "collaborator")
public class Collaborator extends BaseEntity {

    @NotNull
    @ManyToOne
    @JoinColumn(name = "app_user_id")
    private AppUser appUser;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;

    @NotNull
    @Enumerated(EnumType.STRING)
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "collaborator_roles",
            joinColumns = @JoinColumn(name = "collaborator_id")
    )
    @Column(name = "project_roles_set", columnDefinition = "varchar(255)")
    private final Set<ProjectRoles> projectRolesSet = new HashSet<>();

    @ManyToMany
    private final Set<Task> tasks = new HashSet<>();

    @Override
    public String toString() {
        return "Collaborator{" +
                "id=" + id +
                ", tasks=" + getIdsForToString(tasks) +
                ", projectRolesSet=" + projectRolesSet +
                ", projectId=" + (project != null ? project.getId() : null) +
                ",appUserId=" + (appUser != null ? appUser.getId() : null) +
                '}';
    }

    @Converter
    public static class ProjectRolesSetConverter implements AttributeConverter<Set<ProjectRoles>, String> {

        private static final String SEPARATOR = ",";

        @Override
        public String convertToDatabaseColumn(Set<ProjectRoles> attribute) {
            if (attribute == null || attribute.isEmpty()) {
                return "";
            }
            return attribute.stream()
                    .map(ProjectRoles::name)
                    .collect(Collectors.joining(SEPARATOR));
        }

        @Override
        public Set<ProjectRoles> convertToEntityAttribute(String dbData) {
            if (dbData == null || dbData.trim().isEmpty()) {
                return new HashSet<>();
            }
            return Arrays.stream(dbData.split(SEPARATOR))
                    .map(String::trim)
                    .map(ProjectRoles::valueOf)
                    .collect(Collectors.toSet());
        }
    }
}
