
package com.focusforge.focusforge.service;

import com.focusforge.focusforge.dto.TaskStatisticsResponse;
import com.focusforge.focusforge.model.AppUser;
import com.focusforge.focusforge.model.Task;
import com.focusforge.focusforge.model.TaskPriority;
import com.focusforge.focusforge.repository.AppUserRepository;
import com.focusforge.focusforge.repository.TaskRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final AppUserRepository appUserRepository;

    public TaskService(
            TaskRepository taskRepository,
            AppUserRepository appUserRepository) {
        this.taskRepository = taskRepository;
        this.appUserRepository = appUserRepository;
    }

    public List<Task> getAllTasks(Long userId) {
        return taskRepository.findByUser_Id(userId);
    }

    public List<Task> searchTasks(Long userId, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return getAllTasks(userId);
        }

        return taskRepository.findByUser_IdAndTitleContainingIgnoreCase(
                userId, keyword.trim());
    }

    public List<Task> filterTasks(
            Long userId,
            String search,
            Boolean completed,
            TaskPriority priority) {
        return filterTasks(userId, search, completed, priority, "id", "asc");
    }

    public List<Task> filterTasks(
            Long userId,
            String search,
            Boolean completed,
            TaskPriority priority,
            String sortBy,
            String direction) {

        List<Task> tasks = taskRepository.findByUserAndFilters(
                userId, normalizeSearch(search), completed, priority);

        String field = normalizeSortField(sortBy);
        Comparator<Task> comparator = getComparator(field);

        if (direction == null || direction.isBlank()
                || "asc".equalsIgnoreCase(direction)) {
            // Ascending order is the default.
        } else if ("desc".equalsIgnoreCase(direction)) {
            if ("duedate".equals(field)) {
                comparator = Comparator.comparing(
                        Task::getDueDate,
                        Comparator.nullsLast(Comparator.reverseOrder()));
            } else {
                comparator = comparator.reversed();
            }
        } else {
            throw new IllegalArgumentException(
                    "Invalid direction. Use asc or desc.");
        }

        tasks.sort(comparator);
        return tasks;
    }

    public Page<Task> filterTasksPage(
            Long userId,
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

        String keyword = normalizeSearch(search);
        String field = normalizeSortField(sortBy);
        String property = getSortProperty(field);

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

        if ("dueDate".equals(property)) {
            Pageable pageable = PageRequest.of(page, size);

            if (sortDirection == Sort.Direction.ASC) {
                return taskRepository.findByUserAndFiltersDueDateAscending(
                        userId, keyword, completed, priority, pageable);
            }

            return taskRepository.findByUserAndFiltersDueDateDescending(
                    userId, keyword, completed, priority, pageable);
        }

        Sort sort = Sort.by(sortDirection, property)
                .and(Sort.by(Sort.Direction.ASC, "id"));

        Pageable pageable = PageRequest.of(page, size, sort);

        return taskRepository.findByUserAndFilters(
                userId, keyword, completed, priority, pageable);
    }

    private String normalizeSearch(String search) {
        return (search == null || search.isBlank())
                ? null
                : search.trim();
    }

    private String normalizeSortField(String sortBy) {
        return (sortBy == null || sortBy.isBlank())
                ? "id"
                : sortBy.trim().toLowerCase();
    }

    private String getSortProperty(String field) {
        return switch (field) {
            case "id" -> "id";
            case "title" -> "title";
            case "priority" -> "priority";
            case "duedate" -> "dueDate";
            default -> throw new IllegalArgumentException(
                    "Invalid sortBy value. Use id, title, priority, or dueDate.");
        };
    }

    private Comparator<Task> getComparator(String field) {
        return switch (field) {
            case "title" -> Comparator.comparing(
                    Task::getTitle,
                    Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));

            case "priority" -> Comparator.comparing(
                    Task::getPriority,
                    Comparator.nullsLast(Comparator.naturalOrder()));

            case "id" -> Comparator.comparing(
                    Task::getId,
                    Comparator.nullsLast(Comparator.naturalOrder()));

            case "duedate" -> Comparator.comparing(
                    Task::getDueDate,
                    Comparator.nullsLast(Comparator.naturalOrder()));

            default -> throw new IllegalArgumentException(
                    "Invalid sortBy value. Use id, title, priority, or dueDate.");
        };
    }

    @Transactional
    public Task addTask(
            Long userId,
            String title,
            String description,
            TaskPriority priority,
            LocalDate dueDate) {

        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Authenticated user does not exist."));

        Task task = new Task(null, title, false, priority);
        task.setDescription(description);
        task.setDueDate(dueDate);
        task.setUser(user);

        return taskRepository.save(task);
    }

    @Transactional(readOnly = true)
    public Task getTaskById(Long userId, Long id) {
        return taskRepository.findByIdAndUser_Id(id, userId)
                .orElse(null);
    }

    @Transactional
    public Task updateTask(
            Long userId,
            Long id,
            String title,
            String description,
            boolean completed,
            TaskPriority priority,
            LocalDate dueDate) {

        Task task = getTaskById(userId, id);

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

    @Transactional
    public boolean deleteTask(Long userId, Long id) {
        Task task = getTaskById(userId, id);

        if (task == null) {
            return false;
        }

        taskRepository.delete(task);
        return true;
    }

    @Transactional(readOnly = true)
    public TaskStatisticsResponse getTaskStatistics(Long userId) {
        List<Task> tasks = taskRepository.findByUser_Id(userId);

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
