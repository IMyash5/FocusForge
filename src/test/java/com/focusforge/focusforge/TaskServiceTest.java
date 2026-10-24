
package com.focusforge.focusforge;

import com.focusforge.focusforge.model.Task;
import com.focusforge.focusforge.model.TaskPriority;
import com.focusforge.focusforge.repository.TaskRepository;
import com.focusforge.focusforge.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    private TaskService taskService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        taskService = new TaskService(taskRepository);
    }

    @Test
    void shouldAddTaskSuccessfully() {
        LocalDate dueDate = LocalDate.of(2026, 11, 15);

        Task task = new Task(
                null, "Learn Java", false, TaskPriority.HIGH);
        task.setDescription("Practice Java collections");
        task.setDueDate(dueDate);

        when(taskRepository.save(any(Task.class)))
                .thenAnswer(invocation -> {
                    Task savedTask = invocation.getArgument(0);
                    savedTask.setId(1L);
                    return savedTask;
                });

        Task result = taskService.addTask(
                "Learn Java",
                "Practice Java collections",
                TaskPriority.HIGH,
                dueDate);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Learn Java", result.getTitle());
        assertEquals("Practice Java collections", result.getDescription());
        assertEquals(TaskPriority.HIGH, result.getPriority());
        assertEquals(dueDate, result.getDueDate());

        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void shouldFindTaskById() {
        Task task = new Task(
                1L, "Learn Java", false, TaskPriority.MEDIUM);

        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));

        Task result = taskService.getTaskById(1L);

        assertNotNull(result);
        assertEquals("Learn Java", result.getTitle());
    }

    @Test
    void shouldReturnNullWhenTaskDoesNotExist() {
        when(taskRepository.findById(99L))
                .thenReturn(Optional.empty());

        Task result = taskService.getTaskById(99L);

        assertNull(result);
    }

    @Test
    void shouldUpdateTaskSuccessfully() {
        LocalDate dueDate = LocalDate.of(2026, 12, 1);

        Task existingTask = new Task(
                1L, "Old title", false, TaskPriority.LOW);

        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(existingTask));
        when(taskRepository.save(any(Task.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Task result = taskService.updateTask(
                1L,
                "Updated title",
                "Updated description",
                true,
                TaskPriority.HIGH,
                dueDate);

        assertNotNull(result);
        assertEquals("Updated title", result.getTitle());
        assertEquals("Updated description", result.getDescription());
        assertTrue(result.isCompleted());
        assertEquals(TaskPriority.HIGH, result.getPriority());
        assertEquals(dueDate, result.getDueDate());

        verify(taskRepository).save(existingTask);
    }

    @Test
    void shouldReturnNullWhenUpdatingNonexistentTask() {
        when(taskRepository.findById(99L))
                .thenReturn(Optional.empty());

        Task result = taskService.updateTask(
                99L,
                "Updated title",
                "Description",
                false,
                TaskPriority.MEDIUM,
                null);

        assertNull(result);
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void shouldDeleteTaskSuccessfully() {
        when(taskRepository.existsById(1L)).thenReturn(true);

        boolean result = taskService.deleteTask(1L);

        assertTrue(result);
        verify(taskRepository).deleteById(1L);
    }

    @Test
    void shouldReturnFalseWhenDeletingNonexistentTask() {
        when(taskRepository.existsById(99L)).thenReturn(false);

        boolean result = taskService.deleteTask(99L);

        assertFalse(result);
        verify(taskRepository, never()).deleteById(99L);
    }

    @Test
    void shouldPreserveOtherTasksWhenDeletingOneTask() {
        when(taskRepository.existsById(1L)).thenReturn(true);

        boolean result = taskService.deleteTask(1L);

        assertTrue(result);
        verify(taskRepository).deleteById(1L);
    }

    @Test
    void shouldReturnAllTasksSuccessfully() {
        List<Task> tasks = List.of(
                new Task(1L, "Task 1", false),
                new Task(2L, "Task 2", true));

        when(taskRepository.findAll()).thenReturn(tasks);

        List<Task> result = taskService.getAllTasks();

        assertEquals(2, result.size());
        assertEquals("Task 1", result.get(0).getTitle());
        assertEquals("Task 2", result.get(1).getTitle());
    }

    @Test
    void shouldDefaultPriorityToMediumWhenAddingTask() {
        when(taskRepository.save(any(Task.class)))
                .thenAnswer(invocation -> {
                    Task savedTask = invocation.getArgument(0);
                    savedTask.setId(1L);
                    return savedTask;
                });

        Task result = taskService.addTask(
                "Default priority task",
                "Testing default priority",
                null,
                null);

        assertNotNull(result);
        assertEquals(TaskPriority.MEDIUM, result.getPriority());
    }

    @Test
    void shouldSortTasksByDueDateAscendingWithNullDeadlinesLast() {
        Task laterTask = new Task(
                2L, "Later task", false, TaskPriority.MEDIUM);
        laterTask.setDueDate(LocalDate.of(2026, 12, 1));

        Task earlierTask = new Task(
                1L, "Earlier task", false, TaskPriority.HIGH);
        earlierTask.setDueDate(LocalDate.of(2026, 11, 15));

        Task noDeadlineTask = new Task(
                3L, "No deadline", false, TaskPriority.LOW);

        when(taskRepository.findByFilters(null, null, null))
                .thenReturn(new ArrayList<>(
                        List.of(laterTask, noDeadlineTask, earlierTask)));

        List<Task> result = taskService.filterTasks(
                null, null, null, "dueDate", "asc");

        assertEquals(3, result.size());
        assertEquals("Earlier task", result.get(0).getTitle());
        assertEquals("Later task", result.get(1).getTitle());
        assertEquals("No deadline", result.get(2).getTitle());
    }

    @Test
    void shouldSortTasksByDueDateDescendingWithNullDeadlinesLast() {
        Task earlierTask = new Task(
                1L, "Earlier task", false, TaskPriority.HIGH);
        earlierTask.setDueDate(LocalDate.of(2026, 11, 15));

        Task laterTask = new Task(
                2L, "Later task", false, TaskPriority.MEDIUM);
        laterTask.setDueDate(LocalDate.of(2026, 12, 1));

        Task noDeadlineTask = new Task(
                3L, "No deadline", false, TaskPriority.LOW);

        when(taskRepository.findByFilters(null, null, null))
                .thenReturn(new ArrayList<>(
                        List.of(earlierTask, noDeadlineTask, laterTask)));

        List<Task> result = taskService.filterTasks(
                null, null, null, "dueDate", "desc");

        assertEquals(3, result.size());
        assertEquals("Later task", result.get(0).getTitle());
        assertEquals("Earlier task", result.get(1).getTitle());
        assertEquals("No deadline", result.get(2).getTitle());
    }

    @Test
    void shouldRejectInvalidSortField() {
        when(taskRepository.findByFilters(null, null, null))
                .thenReturn(new ArrayList<>());

        assertThrows(
                IllegalArgumentException.class,
                () -> taskService.filterTasks(
                        null, null, null, "invalidField", "asc"));
    }
}
