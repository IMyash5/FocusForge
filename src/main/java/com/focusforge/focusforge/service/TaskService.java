
package com.focusforge.focusforge.service;

import com.focusforge.focusforge.model.Task;
import com.focusforge.focusforge.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    public Task addTask(String title) {
        Task task = new Task(null, title, false);
        return taskRepository.save(task);
    }

    public Task getTaskById(Long id) {
        return taskRepository.findById(id).orElse(null);
    }

    public Task updateTask(Long id, String title, boolean completed) {
        Task task = getTaskById(id);

        if (task == null) {
            return null;
        }

        task.setTitle(title);
        task.setCompleted(completed);

        return taskRepository.save(task);
    }

    public boolean deleteTask(Long id) {
        Task task = getTaskById(id);

        if (task == null) {
            return false;
        }

        taskRepository.delete(task);
        return true;
    }
}
