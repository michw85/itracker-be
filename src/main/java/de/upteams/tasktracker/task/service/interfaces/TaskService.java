package de.upteams.tasktracker.task.service.interfaces;

import de.upteams.tasktracker.task.dto.TaskDto;
import de.upteams.tasktracker.task.entity.Task;
import de.upteams.tasktracker.user.entity.AppUser;

import java.util.List;
import java.util.Optional;

/**
 * Service for various operations with Tasks
 */
public interface TaskService {

    TaskDto save(TaskDto newTaskDto);

    TaskDto getById(String id);

    Task getOrThrow(String id);

    Optional<Task> findById(String id);

    List<TaskDto> getAll(String projectId, AppUser authUser);

    void delete(String id, AppUser changer);

}
