
package com.focusforge.focusforge;

import com.focusforge.focusforge.model.Task;
import com.focusforge.focusforge.repository.TaskRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskRepository taskRepository;

    private Task firstTask;
    private Task secondTask;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();

        firstTask = taskRepository.save(
                new Task(null, "Complete Java assignment", false)
        );

        secondTask = taskRepository.save(
                new Task(null, "Practice Data Structures", false)
        );
    }

    @Test
    void shouldReturnAllTasksWithHttp200() throws Exception {

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title")
                        .value("Complete Java assignment"))
                .andExpect(jsonPath("$[1].title")
                        .value("Practice Data Structures"));
    }

    @Test
    void shouldCreateTaskSuccessfully() throws Exception {

        String requestBody = """
                {
                    "title": "Finish DBMS assignment"
                }
                """;

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.id").value(
                        org.hamcrest.Matchers.greaterThan(0)))
                .andExpect(jsonPath("$.title")
                        .value("Finish DBMS assignment"))
                .andExpect(jsonPath("$.completed").value(false));
    }

    @Test
    void shouldRejectBlankTaskTitle() throws Exception {

        String requestBody = """
                {
                    "title": "   "
                }
                """;

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("Validation failed"))
                .andExpect(jsonPath("$.message")
                        .value("Task title cannot be empty or blank"));
    }

    @Test
    void shouldRejectEmptyTaskTitle() throws Exception {

        String requestBody = """
                {
                    "title": ""
                }
                """;

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("Validation failed"))
                .andExpect(jsonPath("$.message")
                        .value("Task title cannot be empty or blank"));
    }

    @Test
    void shouldReturnTaskById() throws Exception {

        mockMvc.perform(get("/api/tasks/" + firstTask.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(
                        firstTask.getId().intValue()))
                .andExpect(jsonPath("$.title")
                        .value("Complete Java assignment"))
                .andExpect(jsonPath("$.completed").value(false));
    }

    @Test
    void shouldReturn404WhenTaskDoesNotExist() throws Exception {

        mockMvc.perform(get("/api/tasks/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldUpdateTaskSuccessfully() throws Exception {

        String requestBody = """
                {
                    "title": "Finish Java assignment",
                    "completed": true
                }
                """;

        mockMvc.perform(put("/api/tasks/" + firstTask.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(
                        firstTask.getId().intValue()))
                .andExpect(jsonPath("$.title")
                        .value("Finish Java assignment"))
                .andExpect(jsonPath("$.completed").value(true));
    }

    @Test
    void shouldReturn404WhenUpdatingNonexistentTask() throws Exception {

        String requestBody = """
                {
                    "title": "Updated task",
                    "completed": true
                }
                """;

        mockMvc.perform(put("/api/tasks/999999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteTaskSuccessfully() throws Exception {

        Long taskId = firstTask.getId();

        mockMvc.perform(delete("/api/tasks/" + taskId))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/tasks/" + taskId))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn404WhenDeletingNonexistentTask() throws Exception {

        mockMvc.perform(delete("/api/tasks/999999"))
                .andExpect(status().isNotFound());
    }
}
