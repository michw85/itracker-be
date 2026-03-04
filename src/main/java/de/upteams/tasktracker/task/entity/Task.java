package de.upteams.tasktracker.task.entity;

import de.upteams.tasktracker.collaborator.entity.Collaborator;
import de.upteams.tasktracker.project.entity.Project;
import de.upteams.tasktracker.utils.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

import static de.upteams.tasktracker.utils.EntityUtil.getIdForToString;
import static de.upteams.tasktracker.utils.EntityUtil.getIdsForToString;

/**
 * Task entity
 */
@Entity
@Table(name = "task")
@Getter
@Setter
@NoArgsConstructor
public class Task extends BaseEntity {

    @NotBlank
    @Column(name = "title", nullable = false)
    @Pattern(
            regexp = "[A-Z][a-zA-Z1-9 ]{2,}",
            message = "Task title should be at least 3 character length and start with capital letter"
    )
    private String title;

    @Column(name = "description")
    @Pattern(
            regexp = "[A-Z][a-zA-Z1-9,.%:?&!$;*() ]{2,}",
            message = "Task description should be at least 3 character length and start with capital letter"
    )
    private String description;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToMany
    @JoinTable(
            name = "task_user",
            joinColumns = @JoinColumn(name = "task_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private final Set<Collaborator> executors = new HashSet<>();

    public Task(String title, String description, Project project) {
        this.title = title;
        this.description = description;
        this.project = project;
    }

    public Task(String title, Project project) {
        this.title = title;
        this.project = project;
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", executorsIds=" + getIdsForToString(executors) +
                ", projectId=" + getIdForToString(project) +
                ", description='" + description + '\'' +
                ", title='" + title + '\'' +
                '}';
    }
}
