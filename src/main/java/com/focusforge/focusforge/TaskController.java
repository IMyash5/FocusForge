
package com.focusforge.focusforge;

import com.focusforge.focusforge.dto.CreateTaskRequest;
import com.focusforge.focusforge.dto.TaskStatisticsResponse;
import com.focusforge.focusforge.dto.UpdateTaskRequest;
import com.focusforge.focusforge.model.Task;
import com.focusforge.focusforge.model.TaskPriority;
import com.focusforge.focusforge.service.TaskService;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    private Long getUserId(Jwt jwt) {
        try {
            return Long.valueOf(jwt.getSubject());
        } catch (NumberFormatException ex) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid authentication token.");
        }
    }

    @GetMapping("/stats")
    public TaskStatisticsResponse getTaskStatistics(
            @AuthenticationPrincipal Jwt jwt) {

        return taskService.getTaskStatistics(getUserId(jwt));
    }

    @GetMapping
    public Page<Task> getAllTasks(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean completed,
            @RequestParam(required = false) TaskPriority priority,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return taskService.filterTasksPage(
                getUserId(jwt),
                search,
                completed,
                priority,
                sortBy,
                direction,
                page,
                size);
    }

    @GetMapping("/{id}")
    public Task getTaskById(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id) {

        Task task = taskService.getTaskById(getUserId(jwt), id);

        if (task == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Task not found with id: " + id);
        }

        return task;
    }

    @PostMapping
    public ResponseEntity<Task> createTask(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateTaskRequest request) {

        Task createdTask = taskService.addTask(
                getUserId(jwt),
                request.getTitle(),
                request.getDescription(),
                request.getPriority(),
                request.getDueDate());

        URI location = URI.create(
                "/api/tasks/" + createdTask.getId());

        return ResponseEntity
                .created(location)
                .body(createdTask);
    }

    @PutMapping("/{id}")
    public Task updateTask(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id,
            @Valid @RequestBody UpdateTaskRequest request) {

        Task updatedTask = taskService.updateTask(
                getUserId(jwt),
                id,
                request.getTitle(),
                request.getDescription(),
                request.isCompleted(),
                request.getPriority(),
                request.getDueDate());

        if (updatedTask == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Task not found with id: " + id);
        }

        return updatedTask;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id) {

        boolean deleted = taskService.deleteTask(getUserId(jwt), id);

        if (!deleted) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Task not found with id: " + id);
        }

        return ResponseEntity.noContent().build();
    }
}
