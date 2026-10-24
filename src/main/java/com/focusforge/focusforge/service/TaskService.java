
package com.focusforge.focusforge.service;

import com.focusforge.focusforge.model.Task;
import com.focusforge.focusforge.model.TaskPriority;
import com.focusforge.focusforge.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
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

    public List<Task> searchTasks(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return getAllTasks();
        }

        return taskRepository.findByTitleContainingIgnoreCase(
                keyword.trim());
    }

    public List<Task> filterTasks(
            String search,
            Boolean completed,
            TaskPriority priority) {

        return filterTasks(search, completed, priority, "id", "asc");
    }

    public List<Task> filterTasks(
            String search,
            Boolean completed,
            TaskPriority priority,
            String sortBy,
            String direction) {

        String keyword = (search == null || search.isBlank())
                ? null
                : search.trim();

        List<Task> tasks = taskRepository.findByFilters(
                keyword, completed, priority);

        Comparator<Task> comparator;

        String field = (sortBy == null || sortBy.isBlank())
                ? "id"
                : sortBy.trim().toLowerCase();

        switch (field) {
            case "title" ->
                    comparator = Comparator.comparing(
                            Task::getTitle,
                            Comparator.nullsLast(
                                    String.CASE_INSENSITIVE_ORDER));

            case "priority" ->
                    comparator = Comparator.comparing(
                            Task::getPriority,
                            Comparator.nullsLast(
                                    Comparator.naturalOrder()));

            case "id" ->
                    comparator = Comparator.comparing(
                            Task::getId,
                            Comparator.nullsLast(
                                    Comparator.naturalOrder()));

            default ->
                    throw new IllegalArgumentException(
                            "Invalid sortBy value. Use title, priority, or id.");
        }

        if ("desc".equalsIgnoreCase(direction)) {
            comparator = comparator.reversed();
        } else if (direction != null
                && !direction.isBlank()
                && !"asc".equalsIgnoreCase(direction)) {
            throw new IllegalArgumentException(
                    "Invalid direction. Use asc or desc.");
        }

        tasks.sort(comparator);
        return tasks;
    }

    public Task addTask(
            String title,
            String description,
            TaskPriority priority,
            LocalDate dueDate) {

        Task task = new Task(null, title, false, priority);
        task.setDescription(description);
        task.setDueDate(dueDate);

        return taskRepository.save(task);
    }

    public Task getTaskById(Long id) {
        return taskRepository.findById(id).orElse(null);
    }

    public Task updateTask(
            Long id,
            String title,
            String description,
            boolean completed,
            TaskPriority priority,
            LocalDate dueDate) {

        Task task = getTaskById(id);

        if (task == null) {
            return null;
        }

        task.setTitle(title);
        task.setDescription(description);
        task.setCompleted(completed);
        task.setPriority(priority);
        task.setDueDate(dueDate);

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
