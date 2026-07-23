package com.example.employeemanagement.service;

import com.example.employeemanagement.dto.ProjectDTO;
import com.example.employeemanagement.entity.*;
import com.example.employeemanagement.exception.ResourceNotFoundException;
import com.example.employeemanagement.repository.*;
import com.example.employeemanagement.service.impl.ProjectServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ProjectServiceImpl.
 * All repositories and dependent services are mocked — no database required.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProjectService Unit Tests")
class ProjectServiceImplTest {

    @Mock private ProjectRepository projectRepository;
    @Mock private EmployeeRepository employeeRepository;
    @Mock private TaskRepository taskRepository;
    @Mock private UserRepository userRepository;
    @Mock private NotificationService notificationService;

    @InjectMocks
    private ProjectServiceImpl projectService;

    private Project sampleProject;
    private ProjectDTO sampleDto;

    @BeforeEach
    void setUp() {
        sampleProject = Project.builder()
                .id(1L)
                .projectName("Alpha Project")
                .description("A test project")
                .priority("HIGH")
                .status("ACTIVE")
                .startDate(LocalDate.of(2024, 1, 1))
                .endDate(LocalDate.of(2024, 12, 31))
                .build();

        sampleDto = ProjectDTO.builder()
                .projectName("Alpha Project")
                .description("A test project")
                .priority("HIGH")
                .status("ACTIVE")
                .startDate(LocalDate.of(2024, 1, 1))
                .endDate(LocalDate.of(2024, 12, 31))
                .employeeIds(new HashSet<>())
                .build();
    }

    // ─────────────────────────── createProject ───────────────────────────────

    @Test
    @DisplayName("createProject() saves project and notifies admins")
    void createProject_succeeds_andNotifiesAdmins() {
        User admin = User.builder().id(1L).email("admin@company.com").role(Role.ADMIN).build();

        when(projectRepository.save(any(Project.class))).thenReturn(sampleProject);
        when(taskRepository.findByProjectId(1L)).thenReturn(Collections.emptyList());
        when(userRepository.findAll()).thenReturn(List.of(admin));

        ProjectDTO result = projectService.createProject(sampleDto);

        assertThat(result).isNotNull();
        assertThat(result.getProjectName()).isEqualTo("Alpha Project");
        assertThat(result.getStatus()).isEqualTo("ACTIVE");
        verify(notificationService, times(1)).createNotification(
                eq("New Project Created"), anyString(), eq(admin), eq("PROJECT"), eq(1L)
        );
    }

    @Test
    @DisplayName("createProject() throws IllegalArgumentException when end date is before start date")
    void createProject_throwsException_whenEndDateBeforeStartDate() {
        ProjectDTO badDto = ProjectDTO.builder()
                .projectName("Bad Project")
                .priority("LOW")
                .status("ACTIVE")
                .startDate(LocalDate.of(2024, 12, 31))
                .endDate(LocalDate.of(2024, 1, 1)) // End before start
                .employeeIds(new HashSet<>())
                .build();

        assertThatThrownBy(() -> projectService.createProject(badDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("End date cannot be before start date");

        verify(projectRepository, never()).save(any());
    }

    // ─────────────────────────── updateProject ───────────────────────────────

    @Test
    @DisplayName("updateProject() updates project fields and returns updated DTO")
    void updateProject_succeeds_whenProjectExists() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(sampleProject));

        Project updatedProject = Project.builder()
                .id(1L)
                .projectName("Alpha Project Updated")
                .description("Updated description")
                .priority("MEDIUM")
                .status("ACTIVE")
                .startDate(LocalDate.of(2024, 1, 1))
                .endDate(LocalDate.of(2024, 12, 31))
                .build();

        when(projectRepository.save(any(Project.class))).thenReturn(updatedProject);
        when(taskRepository.findByProjectId(1L)).thenReturn(Collections.emptyList());

        ProjectDTO updateDto = ProjectDTO.builder()
                .projectName("Alpha Project Updated")
                .description("Updated description")
                .priority("MEDIUM")
                .status("ACTIVE")
                .startDate(LocalDate.of(2024, 1, 1))
                .endDate(LocalDate.of(2024, 12, 31))
                .build();

        ProjectDTO result = projectService.updateProject(1L, updateDto);

        assertThat(result.getProjectName()).isEqualTo("Alpha Project Updated");
        assertThat(result.getPriority()).isEqualTo("MEDIUM");
    }

    @Test
    @DisplayName("updateProject() throws ResourceNotFoundException when project does not exist")
    void updateProject_throwsResourceNotFoundException_whenNotFound() {
        when(projectRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.updateProject(999L, sampleDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Project not found with id: 999");
    }

    // ─────────────────────────── deleteProject ───────────────────────────────

    @Test
    @DisplayName("deleteProject() deletes the project when found")
    void deleteProject_succeeds_whenProjectExists() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(sampleProject));
        when(taskRepository.findByProjectId(1L)).thenReturn(Collections.emptyList());

        projectService.deleteProject(1L);

        verify(projectRepository).delete(sampleProject);
    }

    @Test
    @DisplayName("deleteProject() throws ResourceNotFoundException when project does not exist")
    void deleteProject_throwsResourceNotFoundException_whenNotFound() {
        when(projectRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.deleteProject(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Project not found with id: 999");

        verify(projectRepository, never()).delete(any(Project.class));
    }

    // ──────────────────────────── getProject ─────────────────────────────────

    @Test
    @DisplayName("getProject() returns DTO when project exists")
    void getProject_returnsDto_whenFound() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(sampleProject));
        when(taskRepository.findByProjectId(1L)).thenReturn(Collections.emptyList());

        ProjectDTO result = projectService.getProject(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getProjectName()).isEqualTo("Alpha Project");
    }

    @Test
    @DisplayName("getProject() throws ResourceNotFoundException when project does not exist")
    void getProject_throwsResourceNotFoundException_whenNotFound() {
        when(projectRepository.findById(55L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.getProject(55L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Project not found with id: 55");
    }

    // ───────────────────────── assignEmployees ────────────────────────────────

    @Test
    @DisplayName("assignEmployees() throws ResourceNotFoundException when an employee ID does not exist")
    void assignEmployees_throwsException_whenEmployeeNotFound() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(sampleProject));
        when(employeeRepository.findAllById(any())).thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> projectService.assignEmployees(1L, Set.of(100L, 200L)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("One or more employees were not found");
    }

    // ───────────────────────── getAllProjects ─────────────────────────────────

    @Test
    @DisplayName("getAllProjects() returns list of all project DTOs")
    void getAllProjects_returnsList() {
        when(projectRepository.findAll()).thenReturn(List.of(sampleProject));
        when(taskRepository.findByProjectId(1L)).thenReturn(Collections.emptyList());

        List<ProjectDTO> result = projectService.getAllProjects();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProjectName()).isEqualTo("Alpha Project");
    }
}
