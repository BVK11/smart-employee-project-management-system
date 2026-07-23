package com.example.employeemanagement.controller;

import com.example.employeemanagement.dto.EmployeeDTO;
import com.example.employeemanagement.dto.PageResponse;
import com.example.employeemanagement.entity.Department;
import com.example.employeemanagement.exception.GlobalExceptionHandler;
import com.example.employeemanagement.exception.ResourceNotFoundException;
import com.example.employeemanagement.service.EmployeeService;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * MockMvc unit tests for EmployeeController using standalone setup.
 * Tests HTTP endpoints, response codes (200, 201, 204, 400, 404), and JSON serialization without Spring context.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EmployeeController Standalone MockMvc Tests")
class EmployeeControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private EmployeeService employeeService;

    @InjectMocks
    private EmployeeController employeeController;

    private EmployeeDTO sampleDto;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        mockMvc = MockMvcBuilders.standaloneSetup(employeeController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        sampleDto = EmployeeDTO.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@company.com")
                .phone("555-1234")
                .department(Department.BACKEND)
                .designation("Senior Developer")
                .salary(80000.0)
                .joiningDate(LocalDate.of(2022, 1, 15))
                .status("ACTIVE")
                .build();
    }

    // ────────────────────────── POST /api/employees ───────────────────────────

    @Test
    @DisplayName("POST /api/employees → HTTP 201 Created")
    void createEmployee_returns201() throws Exception {
        when(employeeService.createEmployee(any(EmployeeDTO.class))).thenReturn(sampleDto);

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.email").value("john.doe@company.com"));
    }

    @Test
    @DisplayName("POST /api/employees → HTTP 400 Bad Request on validation failure")
    void createEmployee_returns400_onInvalidPayload() throws Exception {
        EmployeeDTO invalidDto = EmployeeDTO.builder().build(); // Missing required fields

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    // ────────────────────────── GET /api/employees/{id} ──────────────────────

    @Test
    @DisplayName("GET /api/employees/{id} → HTTP 200 OK")
    void getEmployeeById_returns200_whenFound() throws Exception {
        when(employeeService.getEmployeeById(1L)).thenReturn(sampleDto);

        mockMvc.perform(get("/api/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.lastName").value("Doe"));
    }

    @Test
    @DisplayName("GET /api/employees/{id} → HTTP 404 Not Found")
    void getEmployeeById_returns404_whenNotFound() throws Exception {
        when(employeeService.getEmployeeById(999L))
                .thenThrow(new ResourceNotFoundException("Employee not found with id: 999"));

        mockMvc.perform(get("/api/employees/999"))
                .andExpect(status().isNotFound());
    }

    // ────────────────────────── PUT /api/employees/{id} ──────────────────────

    @Test
    @DisplayName("PUT /api/employees/{id} → HTTP 200 OK")
    void updateEmployee_returns200() throws Exception {
        when(employeeService.updateEmployee(eq(1L), any(EmployeeDTO.class))).thenReturn(sampleDto);

        mockMvc.perform(put("/api/employees/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("John"));
    }

    // ────────────────────────── DELETE /api/employees/{id} ───────────────────

    @Test
    @DisplayName("DELETE /api/employees/{id} → HTTP 204 No Content")
    void deleteEmployee_returns204() throws Exception {
        doNothing().when(employeeService).deleteEmployee(1L);

        mockMvc.perform(delete("/api/employees/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/employees/{id} → HTTP 404 Not Found when non-existent")
    void deleteEmployee_returns404_whenNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Employee not found with id: 999"))
                .when(employeeService).deleteEmployee(999L);

        mockMvc.perform(delete("/api/employees/999"))
                .andExpect(status().isNotFound());
    }

    // ────────────────────────── GET /api/employees ────────────────────────────

    @Test
    @DisplayName("GET /api/employees → HTTP 200 OK with paginated list")
    void getAllEmployees_returns200() throws Exception {
        PageResponse<EmployeeDTO> pageResponse = PageResponse.<EmployeeDTO>builder()
                .content(List.of(sampleDto))
                .pageNo(0).pageSize(10).totalElements(1L).totalPages(1).last(true)
                .build();

        when(employeeService.getAllEmployees(0, 10, "id", "asc")).thenReturn(pageResponse);

        mockMvc.perform(get("/api/employees")
                        .param("page", "0").param("size", "10")
                        .param("sortBy", "id").param("sortDir", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].email").value("john.doe@company.com"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }
}
