package com.example.employeemanagement.controller;

import com.example.employeemanagement.dto.TaskDTO;
import com.example.employeemanagement.exception.GlobalExceptionHandler;
import com.example.employeemanagement.exception.ResourceNotFoundException;
import com.example.employeemanagement.repository.UserRepository;
import com.example.employeemanagement.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * MockMvc unit tests for TaskController using standalone setup.
 * Tests HTTP endpoints, status codes (200, 201, 204, 400, 404), and JSON payloads without Spring context.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TaskController Standalone MockMvc Tests")
class TaskControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock private TaskService taskService;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private TaskController taskController;

    private TaskDTO sampleTaskDto;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        mockMvc = MockMvcBuilders.standaloneSetup(taskController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        sampleTaskDto = TaskDTO.builder()
                .id(1L)
                .title("Write unit tests")
                .description("Cover all service layers")
                .status("PENDING")
                .progress(0)
                .priority("HIGH")
                .deadline(LocalDate.now().plusDays(7))
                .projectId(10L)
                .employeeId(5L)
                .build();
    }

    // ─────────────────────────── POST /api/tasks ─────────────────────────────

    @Test
    @DisplayName("POST /api/tasks → HTTP 201 Created")
    void createTask_returns201() throws Exception {
        when(taskService.createTask(any(TaskDTO.class))).thenReturn(sampleTaskDto);

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleTaskDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Write unit tests"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @DisplayName("POST /api/tasks → HTTP 400 Bad Request on missing required fields")
    void createTask_returns400_onInvalidPayload() throws Exception {
        TaskDTO invalidDto = TaskDTO.builder().build(); // Missing title, status, progress, projectId

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    // ─────────────────────────── GET /api/tasks/{id} ─────────────────────────

    @Test
    @DisplayName("GET /api/tasks/{id} → HTTP 200 OK")
    void getTaskById_returns200_whenFound() throws Exception {
        when(taskService.getTask(1L)).thenReturn(sampleTaskDto);

        mockMvc.perform(get("/api/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Write unit tests"));
    }

    @Test
    @DisplayName("GET /api/tasks/{id} → HTTP 404 Not Found")
    void getTaskById_returns404_whenNotFound() throws Exception {
        when(taskService.getTask(999L))
                .thenThrow(new ResourceNotFoundException("Task not found with id: 999"));

        mockMvc.perform(get("/api/tasks/999"))
                .andExpect(status().isNotFound());
    }

    // ─────────────────────────── PUT /api/tasks/{id} ─────────────────────────

    @Test
    @DisplayName("PUT /api/tasks/{id} → HTTP 200 OK")
    void updateTask_returns200() throws Exception {
        TaskDTO updated = TaskDTO.builder()
                .id(1L).title("Updated task").status("IN_PROGRESS").progress(50)
                .priority("MEDIUM").projectId(10L).employeeId(5L)
                .deadline(LocalDate.now().plusDays(14)).build();

        when(taskService.updateTask(eq(1L), any(TaskDTO.class))).thenReturn(updated);

        mockMvc.perform(put("/api/tasks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleTaskDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated task"))
                .andExpect(jsonPath("$.progress").value(50));
    }

    // ───────────────────────── DELETE /api/tasks/{id} ────────────────────────

    @Test
    @DisplayName("DELETE /api/tasks/{id} → HTTP 204 No Content")
    void deleteTask_returns204() throws Exception {
        doNothing().when(taskService).deleteTask(1L);

        mockMvc.perform(delete("/api/tasks/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/tasks/{id} → HTTP 404 Not Found")
    void deleteTask_returns404_whenNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Task not found with id: 999"))
                .when(taskService).deleteTask(999L);

        mockMvc.perform(delete("/api/tasks/999"))
                .andExpect(status().isNotFound());
    }

    // ─────────────────────── GET /api/tasks ──────────────────────────────────

    @Test
    @DisplayName("GET /api/tasks → HTTP 200 OK with list of tasks")
    void getAllTasks_returns200() throws Exception {
        when(taskService.getAllTasks()).thenReturn(List.of(sampleTaskDto));

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Write unit tests"))
                .andExpect(jsonPath("$").isArray());
    }

    // ─────────────────────── PUT /api/tasks/{id}/status ──────────────────────

    @Test
    @DisplayName("PUT /api/tasks/{id}/status → HTTP 200 OK")
    void updateTaskStatus_returns200() throws Exception {
        TaskDTO completed = TaskDTO.builder()
                .id(1L).title("Write unit tests").status("COMPLETED").progress(100)
                .projectId(10L).employeeId(5L).build();

        when(taskService.updateStatus(eq(1L), eq("COMPLETED"))).thenReturn(completed);

        String body = objectMapper.writeValueAsString(Map.of("status", "COMPLETED"));

        mockMvc.perform(put("/api/tasks/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    // ─────────────────────── PUT /api/tasks/{id}/progress ────────────────────

    @Test
    @DisplayName("PUT /api/tasks/{id}/progress → HTTP 200 OK")
    void updateTaskProgress_returns200() throws Exception {
        TaskDTO progressed = TaskDTO.builder()
                .id(1L).title("Write unit tests").status("IN_PROGRESS").progress(75)
                .projectId(10L).employeeId(5L).build();

        when(taskService.updateProgress(eq(1L), eq(75))).thenReturn(progressed);

        String body = objectMapper.writeValueAsString(Map.of("progress", 75));

        mockMvc.perform(put("/api/tasks/1/progress")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.progress").value(75));
    }
}
