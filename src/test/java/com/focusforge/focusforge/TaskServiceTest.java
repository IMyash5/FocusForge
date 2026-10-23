
package com.focusforge.focusforge;

import com.focusforge.focusforge.model.Task;
import com.focusforge.focusforge.model.TaskPriority;
import com.focusforge.focusforge.repository.TaskRepository;
import com.focusforge.focusforge.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

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
        Task savedTask = new Task(
                3L,
                "Finish DBMS assignment",
                false,
                TaskPriority.HIGH
        );
        savedTask.setDescription("Complete the database assignment");

        when(taskRepository.save(any(Task.class)))
                .thenAnswer(invocation -> {
                    Task task = invocation.getArgument(0);
                    task.setId(3L);
                    return task;
                });

        Task result = taskService.addTask(
                "Finish DBMS assignment",
                "Complete the database assignment",
                TaskPriority.HIGH
        );

        assertNotNull(result);
        assertEquals("Finish DBMS assignment", result.getTitle());
        assertEquals("Complete the database assignment", result.getDescription());
        assertEquals(TaskPriority.HIGH, result.getPriority());
        assertFalse(result.isCompleted());

        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void shouldFindTaskById() {
        Task expectedTask = new Task(1L, "Complete Java assignment", false);

        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(expectedTask));

        Task result = taskService.getTaskById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Complete Java assignment", result.getTitle());
    }

    @Test
    void shouldReturnNullWhenTaskDoesNotExist() {
        when(taskRepository.findById(999L))
                .thenReturn(Optional.empty());

        Task result = taskService.getTaskById(999L);

        assertNull(result);
        verify(taskRepository).findById(999L);
    }

    @Test
    void shouldUpdateTaskSuccessfully() {
        Task existingTask = new Task(1L, "Old title", false);

        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(existingTask));
        when(taskRepository.save(any(Task.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Task result = taskService.updateTask(
                1L,
                "New title",
                "Updated description",
                true,
                TaskPriority.HIGH
        );

        assertNotNull(result);
        assertEquals("New title", result.getTitle());
        assertEquals("Updated description", result.getDescription());
        assertTrue(result.isCompleted());
        assertEquals(TaskPriority.HIGH, result.getPriority());

        verify(taskRepository).findById(1L);
        verify(taskRepository).save(existingTask);
    }

    @Test
    void shouldReturnNullWhenUpdatingNonexistentTask() {
        when(taskRepository.findById(999L))
                .thenReturn(Optional.empty());

        Task result = taskService.updateTask(
                999L,
                "New title",
                "Updated description",
                true,
                TaskPriority.HIGH
        );

        assertNull(result);
        verify(taskRepository).findById(999L);
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void shouldDeleteTaskSuccessfully() {
        Task existingTask = new Task(1L, "Complete Java assignment", false);

        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(existingTask));

        boolean result = taskService.deleteTask(1L);

        assertTrue(result);
        verify(taskRepository).findById(1L);
        verify(taskRepository).delete(existingTask);
    }

    @Test
    void shouldReturnFalseWhenDeletingNonexistentTask() {
        when(taskRepository.findById(999L))
                .thenReturn(Optional.empty());

        boolean result = taskService.deleteTask(999L);

        assertFalse(result);
        verify(taskRepository).findById(999L);
        verify(taskRepository, never()).delete(any(Task.class));
    }

    @Test
    void shouldPreserveOtherTasksWhenDeletingOneTask() {
        Task taskToDelete = new Task(1L, "Task to delete", false);

        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(taskToDelete));

        boolean result = taskService.deleteTask(1L);

        assertTrue(result);
        verify(taskRepository).delete(taskToDelete);
        verify(taskRepository, never()).deleteAll();
        verify(taskRepository, times(1)).delete(taskToDelete);
    }

    @Test
    void shouldReturnAllTasksSuccessfully() {
        List<Task> expectedTasks = List.of(
                new Task(1L, "Complete Java assignment", false),
                new Task(2L, "Practice Data Structures", false)
        );

        when(taskRepository.findAll()).thenReturn(expectedTasks);

        List<Task> result = taskService.getAllTasks();

        assertEquals(2, result.size());
        assertEquals("Complete Java assignment", result.get(0).getTitle());
        assertEquals("Practice Data Structures", result.get(1).getTitle());

        verify(taskRepository).findAll();
    }

    @Test
    void shouldDefaultPriorityToMediumWhenAddingTask() {
        when(taskRepository.save(any(Task.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Task result = taskService.addTask(
                "Read Java notes",
                null,
                null
        );

        assertEquals(TaskPriority.MEDIUM, result.getPriority());
        verify(taskRepository).save(any(Task.class));
    }
}
