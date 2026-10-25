
package com.focusforge.focusforge;

import com.focusforge.focusforge.model.AppUser;
import com.focusforge.focusforge.model.Task;
import com.focusforge.focusforge.model.TaskPriority;
import com.focusforge.focusforge.repository.AppUserRepository;
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

    private static final Long USER_ID = 1L;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private AppUserRepository appUserRepository;

    private TaskService taskService;
    private AppUser testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        taskService = new TaskService(taskRepository, appUserRepository);

        testUser = new AppUser("test@example.com", "test-password-hash");

        // Simulate a user already stored in the database.
        try {
            var idField = AppUser.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(testUser, USER_ID);
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException("Could not prepare test user", ex);
        }

        when(appUserRepository.findById(USER_ID))
                .thenReturn(Optional.of(testUser));
    }

    @Test
    void shouldAddTaskSuccessfully() {
        LocalDate dueDate = LocalDate.of(2026, 11, 15);

        when(taskRepository.save(any(Task.class)))
                .thenAnswer(invocation -> {
                    Task savedTask = invocation.getArgument(0);
                    savedTask.setId(1L);
                    return savedTask;
                });

        Task result = taskService.addTask(
                USER_ID,
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
        assertEquals(USER_ID, result.getUser().getId());

        verify(taskRepository).save(any(Task.class));
        verify(appUserRepository).findById(USER_ID);
    }

    @Test
    void shouldFindTaskByIdForCurrentUser() {
        Task task = new Task(
                1L, "Learn Java", false, TaskPriority.MEDIUM);

        when(taskRepository.findByIdAndUser_Id(1L, USER_ID))
                .thenReturn(Optional.of(task));

        Task result = taskService.getTaskById(USER_ID, 1L);

        assertNotNull(result);
        assertEquals("Learn Java", result.getTitle());
    }

    @Test
    void shouldReturnNullWhenTaskDoesNotExistForCurrentUser() {
        when(taskRepository.findByIdAndUser_Id(99L, USER_ID))
                .thenReturn(Optional.empty());

        Task result = taskService.getTaskById(USER_ID, 99L);

        assertNull(result);
    }

    @Test
    void shouldUpdateTaskSuccessfully() {
        LocalDate dueDate = LocalDate.of(2026, 12, 1);

        Task existingTask = new Task(
                1L, "Old title", false, TaskPriority.LOW);

        when(taskRepository.findByIdAndUser_Id(1L, USER_ID))
                .thenReturn(Optional.of(existingTask));

        when(taskRepository.save(any(Task.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Task result = taskService.updateTask(
                USER_ID,
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
    void shouldReturnNullWhenUpdatingTaskNotOwnedByUser() {
        when(taskRepository.findByIdAndUser_Id(99L, USER_ID))
                .thenReturn(Optional.empty());

        Task result = taskService.updateTask(
                USER_ID,
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
        Task task = new Task(1L, "Task to delete", false);

        when(taskRepository.findByIdAndUser_Id(1L, USER_ID))
                .thenReturn(Optional.of(task));

        boolean result = taskService.deleteTask(USER_ID, 1L);

        assertTrue(result);
        verify(taskRepository).delete(task);
    }

    @Test
    void shouldReturnFalseWhenDeletingTaskNotOwnedByUser() {
        when(taskRepository.findByIdAndUser_Id(99L, USER_ID))
                .thenReturn(Optional.empty());

        boolean result = taskService.deleteTask(USER_ID, 99L);

        assertFalse(result);
        verify(taskRepository, never()).delete(any(Task.class));
    }

    @Test
    void shouldReturnOnlyCurrentUsersTasks() {
        List<Task> tasks = List.of(
                new Task(1L, "Task 1", false),
                new Task(2L, "Task 2", true));

        when(taskRepository.findByUser_Id(USER_ID)).thenReturn(tasks);

        List<Task> result = taskService.getAllTasks(USER_ID);

        assertEquals(2, result.size());
        assertEquals("Task 1", result.get(0).getTitle());
        assertEquals("Task 2", result.get(1).getTitle());

        verify(taskRepository).findByUser_Id(USER_ID);
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
                USER_ID,
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

        when(taskRepository.findByUserAndFilters(USER_ID, null, null, null))
                .thenReturn(new ArrayList<>(
                        List.of(laterTask, noDeadlineTask, earlierTask)));

        List<Task> result = taskService.filterTasks(
                USER_ID, null, null, null, "dueDate", "asc");

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

        when(taskRepository.findByUserAndFilters(USER_ID, null, null, null))
                .thenReturn(new ArrayList<>(
                        List.of(earlierTask, noDeadlineTask, laterTask)));

        List<Task> result = taskService.filterTasks(
                USER_ID, null, null, null, "dueDate", "desc");

        assertEquals(3, result.size());
        assertEquals("Later task", result.get(0).getTitle());
        assertEquals("Earlier task", result.get(1).getTitle());
        assertEquals("No deadline", result.get(2).getTitle());
    }

    @Test
    void shouldRejectInvalidSortField() {
        when(taskRepository.findByUserAndFilters(USER_ID, null, null, null))
                .thenReturn(new ArrayList<>());

        assertThrows(
                IllegalArgumentException.class,
                () -> taskService.filterTasks(
                        USER_ID, null, null, null, "invalidField", "asc"));
    }
}
