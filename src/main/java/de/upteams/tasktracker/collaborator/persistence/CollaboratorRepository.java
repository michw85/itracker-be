package de.upteams.tasktracker.collaborator.persistence;

import de.upteams.tasktracker.collaborator.entity.Collaborator;
import de.upteams.tasktracker.project.entity.Project;
import de.upteams.tasktracker.user.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CollaboratorRepository extends JpaRepository<Collaborator, UUID> {

    @Query("select c from Collaborator c where c.appUser = ?1 and c.project = ?2")
    Optional<Collaborator> findCollaborator(AppUser user, Project project);

}
