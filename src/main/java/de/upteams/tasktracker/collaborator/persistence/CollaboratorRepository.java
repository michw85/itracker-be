package de.upteams.tasktracker.collaborator.persistence;

import de.upteams.tasktracker.collaborator.entity.Collaborator;
import de.upteams.tasktracker.project.entity.Project;
import de.upteams.tasktracker.user.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CollaboratorRepository extends JpaRepository<Collaborator, UUID> {

    List<Collaborator> findByAppUser(AppUser appUser);

    List<Collaborator> findByAppUserId(UUID userId);

    @Query("select c from Collaborator c where c.appUser = ?1 and c.project = ?2")
    Optional<Collaborator> findCollaborator(AppUser user, Project project);

    @Query("SELECT c FROM Collaborator c WHERE c.project.id = :projectId AND c.appUser.id = :userId")
    Optional<Collaborator> findByProjectIdAndUserId(@Param("projectId") UUID projectId, @Param("userId") UUID userId);

    List<Collaborator> findByProjectId(UUID projectId);

    @Query("SELECT c FROM Collaborator c WHERE c.project.id = :projectId")
    List<Collaborator> findMembersByProjectId(@Param("projectId") UUID projectId);

    @Query("SELECT COUNT(c) FROM Collaborator c WHERE c.project.id = :projectId")
    int countByProjectId(@Param("projectId") UUID projectId);
}
