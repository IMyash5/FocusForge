
package com.focusforge.focusforge;

import com.focusforge.focusforge.model.AppUser;
import com.focusforge.focusforge.model.Task;
import com.focusforge.focusforge.repository.AppUserRepository;
import com.focusforge.focusforge.repository.TaskRepository;
import com.focusforge.focusforge.service.JwtService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

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

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private JwtService jwtService;

    private AppUser testUser;
    private Task firstTask;
    private Task secondTask;
    private String token;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();

        testUser = appUserRepository.save(
                new AppUser("test@example.com", "test-password-hash")
        );

        firstTask = new Task(
                null, "Complete Java assignment", false);
        firstTask.setUser(testUser);
        firstTask = taskRepository.save(firstTask);

        secondTask = new Task(
                null, "Practice Data Structures", false);
        secondTask.setUser(testUser);
        secondTask = taskRepository.save(secondTask);

        token = jwtService.generateToken(
                testUser.getId(), testUser.getEmail());
    }

    private RequestPostProcessor withJwt() {
        return request -> {
            request.addHeader("Authorization", "Bearer " + token);
            return request;
        };
    }

    private String tokenFor(AppUser user) {
        return jwtService.generateToken(user.getId(), user.getEmail());
    }

    @Test
    void shouldReturnOnlyCurrentUsersTasks() throws Exception {
        mockMvc.perform(get("/api/tasks").with(withJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].title")
                        .value("Complete Java assignment"))
                .andExpect(jsonPath("$.content[1].title")
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
                        .with(withJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.id")
                        .value(org.hamcrest.Matchers.greaterThan(0)))
                .andExpect(jsonPath("$.title")
                        .value("Finish DBMS assignment"))
                .andExpect(jsonPath("$.completed").value(false))
                .andExpect(jsonPath("$.user").doesNotExist());
    }

    @Test
    void shouldRejectBlankTaskTitle() throws Exception {
        String requestBody = """
                {
                    "title": "   "
                }
                """;

        mockMvc.perform(post("/api/tasks")
                        .with(withJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"))
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
                        .with(withJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.message")
                        .value("Task title cannot be empty or blank"));
    }

    @Test
    void shouldReturnTaskById() throws Exception {
        mockMvc.perform(get("/api/tasks/" + firstTask.getId())
                        .with(withJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(firstTask.getId().intValue()))
                .andExpect(jsonPath("$.title")
                        .value("Complete Java assignment"))
                .andExpect(jsonPath("$.completed").value(false))
                .andExpect(jsonPath("$.user").doesNotExist());
    }

    @Test
    void shouldReturn404WhenTaskDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/tasks/999999").with(withJwt()))
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
                        .with(withJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(firstTask.getId().intValue()))
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
                        .with(withJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteTaskSuccessfully() throws Exception {
        Long taskId = firstTask.getId();

        mockMvc.perform(delete("/api/tasks/" + taskId)
                        .with(withJwt()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/tasks/" + taskId)
                        .with(withJwt()))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn404WhenDeletingNonexistentTask() throws Exception {
        mockMvc.perform(delete("/api/tasks/999999").with(withJwt()))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldNotAllowAnotherUserToReadTask() throws Exception {
        AppUser anotherUser = appUserRepository.save(
                new AppUser("another@example.com", "another-password-hash"));

        String anotherToken = tokenFor(anotherUser);

        mockMvc.perform(get("/api/tasks/" + firstTask.getId())
                        .header("Authorization", "Bearer " + anotherToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldNotAllowAnotherUserToUpdateTask() throws Exception {
        AppUser anotherUser = appUserRepository.save(
                new AppUser("another@example.com", "another-password-hash"));

        String anotherToken = tokenFor(anotherUser);

        String requestBody = """
                {
                    "title": "Hijacked task",
                    "completed": true
                }
                """;

        mockMvc.perform(put("/api/tasks/" + firstTask.getId())
                        .header("Authorization", "Bearer " + anotherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound());

        Task unchangedTask = taskRepository.findById(firstTask.getId())
                .orElseThrow();

        org.junit.jupiter.api.Assertions.assertEquals(
                "Complete Java assignment", unchangedTask.getTitle());
        org.junit.jupiter.api.Assertions.assertFalse(
                unchangedTask.isCompleted());
    }

    @Test
    void shouldNotAllowAnotherUserToDeleteTask() throws Exception {
        AppUser anotherUser = appUserRepository.save(
                new AppUser("another@example.com", "another-password-hash"));

        String anotherToken = tokenFor(anotherUser);

        mockMvc.perform(delete("/api/tasks/" + firstTask.getId())
                        .header("Authorization", "Bearer " + anotherToken))
                .andExpect(status().isNotFound());

        org.junit.jupiter.api.Assertions.assertTrue(
                taskRepository.existsById(firstTask.getId()));
    }

    @Test
    void shouldNotShowAnotherUsersTasksInList() throws Exception {
        AppUser anotherUser = appUserRepository.save(
                new AppUser("another@example.com", "another-password-hash"));

        Task privateTask = new Task(
                null, "Another user's private task", false);
        privateTask.setUser(anotherUser);
        taskRepository.save(privateTask);

        mockMvc.perform(get("/api/tasks").with(withJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath(
                        "$.content[?(@.title == 'Another user\\u0027s private task')]")
                        .doesNotExist());
    }

    @Test
    void shouldReturnStatisticsForCurrentUserOnly() throws Exception {
        AppUser anotherUser = appUserRepository.save(
                new AppUser("another@example.com", "another-password-hash"));

        Task privateTask = new Task(
                null, "Another user's private task", false);
        privateTask.setUser(anotherUser);
        taskRepository.save(privateTask);

        mockMvc.perform(get("/api/tasks/stats").with(withJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTasks").value(2))
                .andExpect(jsonPath("$.completedTasks").value(0))
                .andExpect(jsonPath("$.pendingTasks").value(2));
    }
}
