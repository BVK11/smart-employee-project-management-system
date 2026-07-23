package com.example.employeemanagement.service;

import com.example.employeemanagement.dto.TaskDTO;
import com.example.employeemanagement.entity.*;
import com.example.employeemanagement.exception.ResourceNotFoundException;
import com.example.employeemanagement.repository.*;
import com.example.employeemanagement.service.impl.TaskServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TaskServiceImpl.
 * All repositories and dependencies are mocked — no database connection required.
 * SecurityContextHolder is mocked to simulate authenticated users.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TaskService Unit Tests")
class TaskServiceImplTest {

    @Mock private TaskRepository taskRepository;
    @Mock private EmployeeRepository employeeRepository;
    @Mock private ProjectRepository projectRepository;
    @Mock private UserRepository userRepository;
    @Mock private NotificationService notificationService;

    @InjectMocks
    private TaskServiceImpl taskService;

    private Task sampleTask;
    private TaskDTO sampleDto;
    private Project sampleProject;
    private Employee sampleEmployee;
    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .id(2L).email("emp@company.com").role(Role.EMPLOYEE).build();

        sampleProject = Project.builder()
                .id(10L).projectName("Beta Project").status("ACTIVE")
                .startDate(LocalDate.now()).endDate(LocalDate.now().plusMonths(6))
                .employees(new HashSet<>())
                .build();

        Set<Project> projectSet = new HashSet<>();
        projectSet.add(sampleProject);

        sampleEmployee = Employee.builder()
                .id(5L).firstName("Alice").lastName("Smith")
                .email("emp@company.com").user(sampleUser)
                .projects(projectSet)
                .build();

        sampleProject.getEmployees().add(sampleEmployee);

        sampleTask = Task.builder()
                .id(1L).title("Write unit tests").description("Cover all services")
                .status("PENDING").progress(0).priority("HIGH")
                .deadline(LocalDate.now().plusDays(7))
                .assignedDate(LocalDate.now())
                .employee(sampleEmployee).project(sampleProject)
                .build();

        sampleDto = TaskDTO.builder()
                .title("Write unit tests").description("Cover all services")
                .status("PENDING").progress(0).priority("HIGH")
                .deadline(LocalDate.now().plusDays(7))
                .projectId(10L).employeeId(5L)
                .build();
    }

    /** Sets up a mock SecurityContext that returns the given email as the principal. */
    private MockedStatic<SecurityContextHolder> mockSecurityContext(String email) {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn(email);
        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);
        MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class);
        mocked.when(SecurityContextHolder::getContext).thenReturn(context);
        return mocked;
    }

    // ─────────────────────────── createTask ──────────────────────────────────

    @Test
    @DisplayName("createTask() saves task and notifies the assigned employee")
    void createTask_succeeds_andNotifiesEmployee() {
        when(employeeRepository.findById(5L)).thenReturn(Optional.of(sampleEmployee));
        when(projectRepository.findById(10L)).thenReturn(Optional.of(sampleProject));
        when(taskRepository.save(any(Task.class))).thenReturn(sampleTask);

        TaskDTO result = taskService.createTask(sampleDto);

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Write unit tests");
        verify(notificationService).createNotification(
                eq("New Task Assigned"), anyString(), eq(sampleUser), eq("TASK"), eq(1L)
        );
    }

    @Test
    @DisplayName("createTask() throws ResourceNotFoundException when project does not exist")
    void createTask_throwsException_whenProjectNotFound() {
        when(projectRepository.findById(999L)).thenReturn(Optional.empty());

        TaskDTO dto = TaskDTO.builder()
                .title("Task").status("PENDING").progress(0)
                .projectId(999L).build();

        assertThatThrownBy(() -> taskService.createTask(dto))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ─────────────────────────── updateTask ──────────────────────────────────

    @Test
    @DisplayName("updateTask() updates task fields and returns updated DTO")
    void updateTask_succeeds_whenTaskExists() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(sampleTask));
        when(employeeRepository.findById(5L)).thenReturn(Optional.of(sampleEmployee));
        when(projectRepository.findById(10L)).thenReturn(Optional.of(sampleProject));

        Task updatedTask = Task.builder()
                .id(1L).title("Updated title").status("IN_PROGRESS").progress(50)
                .priority("MEDIUM").employee(sampleEmployee).project(sampleProject)
                .deadline(LocalDate.now().plusDays(14))
                .assignedDate(LocalDate.now())
                .build();

        when(taskRepository.save(any(Task.class))).thenReturn(updatedTask);

        TaskDTO updateDto = TaskDTO.builder()
                .title("Updated title").status("IN_PROGRESS").progress(50)
                .priority("MEDIUM").projectId(10L).employeeId(5L)
                .deadline(LocalDate.now().plusDays(14)).build();

        TaskDTO result = taskService.updateTask(1L, updateDto);

        assertThat(result.getTitle()).isEqualTo("Updated title");
        assertThat(result.getProgress()).isEqualTo(50);
    }

    @Test
    @DisplayName("updateTask() throws ResourceNotFoundException when task does not exist")
    void updateTask_throwsResourceNotFoundException_whenNotFound() {
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.updateTask(999L, sampleDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Task not found with id: 999");
    }

    // ─────────────────────────── deleteTask ──────────────────────────────────

    @Test
    @DisplayName("deleteTask() deletes task when found")
    void deleteTask_succeeds_whenTaskExists() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(sampleTask));

        taskService.deleteTask(1L);

        verify(taskRepository).delete(sampleTask);
    }

    @Test
    @DisplayName("deleteTask() throws ResourceNotFoundException when task does not exist")
    void deleteTask_throwsResourceNotFoundException_whenNotFound() {
        when(taskRepository.findById(77L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.deleteTask(77L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Task not found with id: 77");

        verify(taskRepository, never()).delete(any(Task.class));
    }

    // ─────────────────────── updateProgress (completion) ─────────────────────

    @Test
    @DisplayName("updateProgress(100) marks task as COMPLETED and sets completedDate")
    void updateProgress_100_completesTask() {
        try (MockedStatic<SecurityContextHolder> mocked = mockSecurityContext("admin@company.com")) {
            User adminUser = User.builder().id(1L).email("admin@company.com").role(Role.ADMIN).build();
            when(userRepository.findByEmail("admin@company.com")).thenReturn(Optional.of(adminUser));
            when(taskRepository.findById(1L)).thenReturn(Optional.of(sampleTask));

            Task completedTask = Task.builder()
                    .id(1L).title("Write unit tests").status("COMPLETED").progress(100)
                    .completedDate(LocalDate.now()).employee(sampleEmployee).project(sampleProject)
                    .build();
            when(taskRepository.save(any(Task.class))).thenReturn(completedTask);

            TaskDTO result = taskService.updateProgress(1L, 100);

            assertThat(result.getStatus()).isEqualTo("COMPLETED");
            assertThat(result.getProgress()).isEqualTo(100);
        }
    }

    @Test
    @DisplayName("updateProgress() throws IllegalArgumentException for invalid progress value")
    void updateProgress_throwsException_forInvalidValue() {
        assertThatThrownBy(() -> taskService.updateProgress(1L, 150))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Progress must be between 0 and 100");
    }

    // ────────────────────────── updateStatus ─────────────────────────────────

    @Test
    @DisplayName("updateStatus() to COMPLETED sets progress to 100")
    void updateStatus_toCompleted_setsProgress100() {
        try (MockedStatic<SecurityContextHolder> mocked = mockSecurityContext("admin@company.com")) {
            User adminUser = User.builder().id(1L).email("admin@company.com").role(Role.ADMIN).build();
            when(userRepository.findByEmail("admin@company.com")).thenReturn(Optional.of(adminUser));
            when(taskRepository.findById(1L)).thenReturn(Optional.of(sampleTask));

            Task completedTask = Task.builder()
                    .id(1L).title("Write unit tests").status("COMPLETED").progress(100)
                    .completedDate(LocalDate.now()).employee(sampleEmployee).project(sampleProject)
                    .build();
            when(taskRepository.save(any(Task.class))).thenReturn(completedTask);

            TaskDTO result = taskService.updateStatus(1L, "COMPLETED");

            assertThat(result.getStatus()).isEqualTo("COMPLETED");
            assertThat(result.getProgress()).isEqualTo(100);
        }
    }

    @Test
    @DisplayName("updateStatus() throws IllegalArgumentException for invalid status value")
    void updateStatus_throwsException_forInvalidStatus() {
        assertThatThrownBy(() -> taskService.updateStatus(1L, "INVALID_STATUS"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
