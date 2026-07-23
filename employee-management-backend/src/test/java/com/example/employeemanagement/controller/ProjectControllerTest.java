package com.example.employeemanagement.controller;

import com.example.employeemanagement.dto.ProjectDTO;
import com.example.employeemanagement.entity.Role;
import com.example.employeemanagement.entity.User;
import com.example.employeemanagement.exception.GlobalExceptionHandler;
import com.example.employeemanagement.exception.ResourceNotFoundException;
import com.example.employeemanagement.repository.UserRepository;
import com.example.employeemanagement.service.ProjectService;
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

import java.security.Principal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * MockMvc unit tests for ProjectController using standalone setup.
 * Tests HTTP endpoints, status codes (200, 201, 204, 400, 404), and JSON payloads without Spring context.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProjectController Standalone MockMvc Tests")
class ProjectControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock private ProjectService projectService;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private ProjectController projectController;

    private ProjectDTO sampleProjectDto;
    private User adminUser;
    private Principal mockPrincipal;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        mockMvc = MockMvcBuilders.standaloneSetup(projectController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        sampleProjectDto = ProjectDTO.builder()
                .id(1L)
                .projectName("Alpha Project")
                .description("A test project")
                .priority("HIGH")
                .status("ACTIVE")
                .startDate(LocalDate.of(2024, 1, 1))
                .endDate(LocalDate.of(2024, 12, 31))
                .employeeIds(new HashSet<>())
                .progress(0.0)
                .build();

        adminUser = User.builder().id(1L).email("admin@company.com").role(Role.ADMIN).build();
        mockPrincipal = () -> "admin@company.com";
    }

    // ───────────────────────── POST /api/projects ────────────────────────────

    @Test
    @DisplayName("POST /api/projects → HTTP 201 Created")
    void createProject_returns201() throws Exception {
        when(projectService.createProject(any(ProjectDTO.class))).thenReturn(sampleProjectDto);

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleProjectDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.projectName").value("Alpha Project"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("POST /api/projects → HTTP 400 Bad Request on missing fields")
    void createProject_returns400_onInvalidPayload() throws Exception {
        ProjectDTO invalidDto = ProjectDTO.builder().build(); // Missing required projectName, priority, status

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    // ─────────────────────── GET /api/projects/{id} ──────────────────────────

    @Test
    @DisplayName("GET /api/projects/{id} → HTTP 200 OK")
    void getProject_returns200_whenFound() throws Exception {
        when(userRepository.findByEmail("admin@company.com")).thenReturn(Optional.of(adminUser));
        when(projectService.getProject(1L)).thenReturn(sampleProjectDto);

        mockMvc.perform(get("/api/projects/1").principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.projectName").value("Alpha Project"));
    }

    @Test
    @DisplayName("GET /api/projects/{id} → HTTP 404 Not Found")
    void getProject_returns404_whenNotFound() throws Exception {
        when(userRepository.findByEmail("admin@company.com")).thenReturn(Optional.of(adminUser));
        when(projectService.getProject(999L))
                .thenThrow(new ResourceNotFoundException("Project not found with id: 999"));

        mockMvc.perform(get("/api/projects/999").principal(mockPrincipal))
                .andExpect(status().isNotFound());
    }

    // ─────────────────────── PUT /api/projects/{id} ──────────────────────────

    @Test
    @DisplayName("PUT /api/projects/{id} → HTTP 200 OK")
    void updateProject_returns200() throws Exception {
        when(projectService.updateProject(eq(1L), any(ProjectDTO.class))).thenReturn(sampleProjectDto);

        mockMvc.perform(put("/api/projects/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleProjectDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectName").value("Alpha Project"));
    }

    @Test
    @DisplayName("PUT /api/projects/{id} → HTTP 404 Not Found")
    void updateProject_returns404_whenNotFound() throws Exception {
        when(projectService.updateProject(eq(999L), any(ProjectDTO.class)))
                .thenThrow(new ResourceNotFoundException("Project not found with id: 999"));

        mockMvc.perform(put("/api/projects/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleProjectDto)))
                .andExpect(status().isNotFound());
    }

    // ─────────────────────── DELETE /api/projects/{id} ───────────────────────

    @Test
    @DisplayName("DELETE /api/projects/{id} → HTTP 204 No Content")
    void deleteProject_returns204() throws Exception {
        doNothing().when(projectService).deleteProject(1L);

        mockMvc.perform(delete("/api/projects/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/projects/{id} → HTTP 404 Not Found")
    void deleteProject_returns404_whenNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Project not found with id: 888"))
                .when(projectService).deleteProject(888L);

        mockMvc.perform(delete("/api/projects/888"))
                .andExpect(status().isNotFound());
    }
}
