package de.upteams.tasktracker.task.controller;

import de.upteams.tasktracker.security.service.AuthUserDetails;
import de.upteams.tasktracker.task.controller.api.TaskApi;
import de.upteams.tasktracker.task.dto.TaskDto;
import de.upteams.tasktracker.task.service.interfaces.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TaskController implements TaskApi {

    /**
     * Service for various operations with Tasks
     */
    private final TaskService service;

    @Override
    public TaskDto save(
            TaskDto task,
            AuthUserDetails principal
    ) {
        return service.save(task);
    }

    @Override
    public TaskDto getById(
            String id,
            AuthUserDetails principal
    ) {
        return service.getById(id);
    }

    @Override
    public List<TaskDto> getAll(
            String projectId,
            AuthUserDetails principal
    ) {
        return service.getAll(projectId, principal.user());
    }

    @Override
    public void deleteById(
            String id,
            AuthUserDetails principal
    ) {
        service.delete(id, principal.user());
    }
}
