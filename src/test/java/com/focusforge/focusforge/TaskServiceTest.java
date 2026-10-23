
package com.focusforge.focusforge;

import com.focusforge.focusforge.model.Task;
import com.focusforge.focusforge.service.TaskService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import com.focusforge.focusforge.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
    Task task = new Task(3L, "Finish DBMS assignment", false);

    when(taskRepository.save(any(Task.class))).thenReturn(task);

    Task result = taskService.addTask("Finish DBMS assignment");

    assertNotNull(result);
    assertEquals("Finish DBMS assignment", result.getTitle());
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

    Task result = taskService.updateTask(1L, "New title", true);

    assertNotNull(result);
    assertEquals("New title", result.getTitle());
    assertTrue(result.isCompleted());

    verify(taskRepository).findById(1L);
    verify(taskRepository).save(existingTask);
}


    
@Test
void shouldReturnNullWhenUpdatingNonexistentTask() {
    when(taskRepository.findById(999L))
            .thenReturn(Optional.empty());

    Task result = taskService.updateTask(999L, "New title", true);

    assertNull(result);
    verify(taskRepository).findById(999L);
    verify(taskRepository, never()).save(any(Task.class));
}


    
@Test
void shouldDeleteTaskSuccessfully() {
    Task existingTask = new Task(1L, "Complete Java assignment", false);

    when(taskRepository.findById(1L))
            .thenReturn(Optional.of(existingTask));

    doNothing().when(taskRepository).delete(existingTask);

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


}
