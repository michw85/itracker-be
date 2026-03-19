package de.upteams.tasktracker.project.entity;

import de.upteams.tasktracker.collaborator.entity.Collaborator;
import de.upteams.tasktracker.project.constants.ProjectValidationConstants;
import de.upteams.tasktracker.task.entity.Task;
import de.upteams.tasktracker.user.entity.AppUser;
import de.upteams.tasktracker.utils.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.hibernate.validator.constraints.Length;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static de.upteams.tasktracker.utils.EntityUtil.getIdForToString;
import static de.upteams.tasktracker.utils.EntityUtil.getIdsForToString;

/**
 * Project entity
 */
@Entity
@Table(name = "project")
@NoArgsConstructor
@Getter
@Setter
public class Project extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "title", nullable = false)
    @NotBlank
    @Length(min = ProjectValidationConstants.NAME_MIN_LENGTH, max = ProjectValidationConstants.NAME_MAX_LENGTH)
    @Pattern(
            regexp = ProjectValidationConstants.NAME_REGEX,
            message = "Project title should be at least 3 character length and start with capital letter"
    )
    private String title;

    @Column(name = "description")
//    @Pattern(
//            regexp = "[A-Z][a-zA-Z1-9,.%:?&!$;*() ]{2,}",
//            message = "Project description should be at least 3 character length and start with capital letter"
//    )
    @Size(min = 3, message = "Description must be at least 3 characters")
    private String description;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser owner;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    private Set<Collaborator> projectTeam = new HashSet<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    private final Set<Task> tasks = new HashSet<>();

    public Project(String title, String description, AppUser owner) {
        this.title = title;
        this.description = description;
        this.owner = owner;
    }

    @Override
    public String toString() {
        return "Project{" +
                "id=" + getId() +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", authorId=" + getIdForToString(owner) +
                ", tasksIds=" + getIdsForToString(tasks) +
                '}';
    }
}
