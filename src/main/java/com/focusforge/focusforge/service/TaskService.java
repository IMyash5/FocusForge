
package com.focusforge.focusforge.service;

import com.focusforge.focusforge.dto.TaskStatisticsResponse;
import com.focusforge.focusforge.model.Task;
import com.focusforge.focusforge.model.TaskPriority;
import com.focusforge.focusforge.repository.TaskRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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

        return filterTasks(
                search, completed, priority, "id", "asc");
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

    String field = (sortBy == null || sortBy.isBlank())
            ? "id"
            : sortBy.trim().toLowerCase();

    java.util.Comparator<Task> comparator;

    switch (field) {
        case "title" ->
                comparator = java.util.Comparator.comparing(
                        Task::getTitle,
                        java.util.Comparator.nullsLast(
                                String.CASE_INSENSITIVE_ORDER));

        case "priority" ->
                comparator = java.util.Comparator.comparing(
                        Task::getPriority,
                        java.util.Comparator.nullsLast(
                                java.util.Comparator.naturalOrder()));

        case "id" ->
                comparator = java.util.Comparator.comparing(
                        Task::getId,
                        java.util.Comparator.nullsLast(
                                java.util.Comparator.naturalOrder()));

        case "duedate" ->
                comparator = java.util.Comparator.comparing(
                        Task::getDueDate,
                        java.util.Comparator.nullsLast(
                                java.util.Comparator.naturalOrder()));

        default ->
                throw new IllegalArgumentException(
                        "Invalid sortBy value. Use id, title, priority, or dueDate.");
    }

    boolean descending = "desc".equalsIgnoreCase(direction);

    if (descending) {
        if ("duedate".equals(field)) {
            comparator = java.util.Comparator.comparing(
                    Task::getDueDate,
                    java.util.Comparator.nullsLast(
                            java.util.Comparator.reverseOrder()));
        } else {
            comparator = comparator.reversed();
        }
    } else if (direction != null
            && !direction.isBlank()
            && !"asc".equalsIgnoreCase(direction)) {
        throw new IllegalArgumentException(
                "Invalid direction. Use asc or desc.");
    }

    tasks.sort(comparator);
    return tasks;
}

    public Page<Task> filterTasksPage(
            String search,
            Boolean completed,
            TaskPriority priority,
            String sortBy,
            String direction,
            int page,
            int size) {

        if (page < 0) {
            throw new IllegalArgumentException(
                    "Page number cannot be negative.");
        }

        if (size < 1 || size > 100) {
            throw new IllegalArgumentException(
                    "Page size must be between 1 and 100.");
        }

        String keyword = (search == null || search.isBlank())
                ? null
                : search.trim();

        String field = (sortBy == null || sortBy.isBlank())
                ? "id"
                : sortBy.trim().toLowerCase();

        String property;

        switch (field) {
            case "id" -> property = "id";
            case "title" -> property = "title";
            case "priority" -> property = "priority";
            case "duedate" -> property = "dueDate";
            default -> throw new IllegalArgumentException(
                    "Invalid sortBy value. Use id, title, priority, or dueDate.");
        }

        Sort.Direction sortDirection;

        if (direction == null || direction.isBlank()
                || "asc".equalsIgnoreCase(direction)) {
            sortDirection = Sort.Direction.ASC;
        } else if ("desc".equalsIgnoreCase(direction)) {
            sortDirection = Sort.Direction.DESC;
        } else {
            throw new IllegalArgumentException(
                    "Invalid direction. Use asc or desc.");
        }

        Sort sort;

        if ("dueDate".equals(property)) {
            sort = Sort.by(
                    sortDirection,
                    property).and(
                    Sort.by(Sort.Direction.ASC, "id"));
        } else {
            sort = Sort.by(sortDirection, property);
        }

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Task> result = taskRepository.findByFilters(
                keyword,
                completed,
                priority,
                pageable);

        if ("dueDate".equals(property)) {
            List<Task> orderedTasks = result.getContent();

            if (sortDirection == Sort.Direction.ASC) {
                orderedTasks.sort(
                        java.util.Comparator.comparing(
                                Task::getDueDate,
                                java.util.Comparator.nullsLast(
                                        java.util.Comparator.naturalOrder())));
            } else {
                orderedTasks.sort(
                        java.util.Comparator.comparing(
                                Task::getDueDate,
                                java.util.Comparator.nullsLast(
                                        java.util.Comparator.reverseOrder())));
            }
        }

        return result;
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
        if (!taskRepository.existsById(id)) {
            return false;
        }

        taskRepository.deleteById(id);
        return true;
    }

    public TaskStatisticsResponse getTaskStatistics() {
        List<Task> tasks = taskRepository.findAll();

        long totalTasks = tasks.size();

        long completedTasks = tasks.stream()
                .filter(Task::isCompleted)
                .count();

        long pendingTasks = totalTasks - completedTasks;

        double completionPercentage = totalTasks == 0
                ? 0.0
                : Math.round(
                        (completedTasks * 100.0 / totalTasks) * 100.0
                ) / 100.0;

        return new TaskStatisticsResponse(
                totalTasks,
                completedTasks,
                pendingTasks,
                completionPercentage);
    }
}
