package de.upteams.tasktracker.project.persistence;

import de.upteams.tasktracker.project.entity.Project;
import de.upteams.tasktracker.task.entity.TaskStatus;
import de.upteams.tasktracker.user.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {

    // Find projects where the user is the owner
    List<Project> findByOwner(AppUser owner);

    // Or with UUID

    @Query("SELECT p FROM Project p WHERE p.owner.id = :userId")
    List<Project> findByOwnerId(@Param("userId") UUID userId);

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Project p WHERE p.id = :projectId AND p.owner.id = :userId")
    boolean isUserOwner(@Param("projectId") UUID projectId, @Param("userId") UUID userId);

    // CollaboratorRepository.java
    @Query("SELECT COUNT(c) FROM Collaborator c WHERE c.project.id = :projectId")
    int countByProjectId(@Param("projectId") UUID projectId);
}