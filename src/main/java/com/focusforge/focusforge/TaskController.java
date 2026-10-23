
package com.focusforge.focusforge;

import org.springframework.web.bind.annotation.DeleteMapping;
import com.focusforge.focusforge.dto.UpdateTaskRequest;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.PathVariable;
import jakarta.validation.Valid;
import com.focusforge.focusforge.dto.CreateTaskRequest;
import com.focusforge.focusforge.model.Task;
import com.focusforge.focusforge.service.TaskService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public List<Task> getAllTasks() {
        return taskService.getAllTasks();
    }
    
    @GetMapping("/{id}")
    public Task getTaskById(@PathVariable Long id) {
        Task task = taskService.getTaskById(id);

        if (task == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Task not found with id: " + id
            );
        }

        return task;
    }
    
    @PutMapping("/{id}")
    public Task updateTask(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTaskRequest request) {

        Task updatedTask = taskService.updateTask(
                id,
                request.getTitle(),
                request.isCompleted()
        );

        if (updatedTask == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Task not found with id: " + id
            );
        }

        return updatedTask;
    }

    @DeleteMapping("/{id}")
    public void deleteTask(@PathVariable Long id) {
        boolean deleted = taskService.deleteTask(id);

        if (!deleted) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Task not found with id: " + id
            );
        }
    }


    @PostMapping
    public Task createTask(@Valid @RequestBody CreateTaskRequest request) {
        return taskService.addTask(request.getTitle());
    }

}
