package com.example.employeemanagement.service.impl;

import com.example.employeemanagement.dto.TaskDTO;
import com.example.employeemanagement.entity.*;
import com.example.employeemanagement.exception.ResourceNotFoundException;
import com.example.employeemanagement.repository.*;
import com.example.employeemanagement.service.TaskService;
import com.example.employeemanagement.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final EmployeeRepository employeeRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Override
    public TaskDTO createTask(TaskDTO dto) {
        Task task = Task.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .status(dto.getStatus())
                .progress(dto.getProgress())
                .priority(dto.getPriority())
                .remarks(dto.getRemarks())
                .deadline(dto.getDeadline())
                .assignedDate(dto.getAssignedDate() != null ? dto.getAssignedDate() : LocalDate.now())
                .dueDate(dto.getDueDate() != null ? dto.getDueDate() : dto.getDeadline())
                .estimatedHours(dto.getEstimatedHours())
                .build();

        if ("COMPLETED".equals(dto.getStatus())) {
            task.setCompletedDate(dto.getCompletedDate() != null ? dto.getCompletedDate() : LocalDate.now());
            task.setProgress(100);
        }

        assign(task, dto.getEmployeeId(), dto.getProjectId());
        Task saved = taskRepository.save(task);

        // Notify Employee if assigned
        if (saved.getEmployee() != null && saved.getEmployee().getUser() != null) {
            notificationService.createNotification(
                    "New Task Assigned",
                    "You have been assigned task \"" + saved.getTitle() + "\" in project \"" + saved.getProject().getProjectName() + "\"",
                    saved.getEmployee().getUser(),
                    "TASK",
                    saved.getId()
            );
        }

        return map(saved);
    }

    @Override
    public TaskDTO updateTask(Long id, TaskDTO dto) {
        Task t = find(id);
        
        // Track original assignment status to see if it changed
        Employee oldEmployee = t.getEmployee();
        String oldStatus = t.getStatus();

        t.setTitle(dto.getTitle());
        t.setDescription(dto.getDescription());
        t.setStatus(dto.getStatus());
        t.setProgress(dto.getProgress());
        t.setPriority(dto.getPriority());
        t.setRemarks(dto.getRemarks());
        t.setDeadline(dto.getDeadline());
        t.setDueDate(dto.getDueDate() != null ? dto.getDueDate() : dto.getDeadline());
        t.setEstimatedHours(dto.getEstimatedHours());

        if ("COMPLETED".equals(dto.getStatus())) {
            if (!"COMPLETED".equals(oldStatus)) {
                t.setCompletedDate(LocalDate.now());
                t.setProgress(100);
            }
        } else {
            t.setCompletedDate(null);
        }

        assign(t, dto.getEmployeeId(), dto.getProjectId());
        Task saved = taskRepository.save(t);

        // Notify Employee if newly assigned or assignment changed
        if (saved.getEmployee() != null && (oldEmployee == null || !oldEmployee.getId().equals(saved.getEmployee().getId()))) {
            if (saved.getEmployee().getUser() != null) {
                notificationService.createNotification(
                        "New Task Assigned",
                        "You have been assigned task \"" + saved.getTitle() + "\" in project \"" + saved.getProject().getProjectName() + "\"",
                        saved.getEmployee().getUser(),
                        "TASK",
                        saved.getId()
                );
            }
        }

        // Notify admins if status changed to completed
        if ("COMPLETED".equals(saved.getStatus()) && !"COMPLETED".equals(oldStatus)) {
            triggerCompletionNotification(saved);
        }

        return map(saved);
    }

    @Override
    public void deleteTask(Long id) {
        taskRepository.delete(find(id));
    }

    @Override
    public TaskDTO assignTask(Long id, Long employeeId, Long projectId) {
        Task t = find(id);
        Employee oldEmployee = t.getEmployee();
        assign(t, employeeId, projectId);
        Task saved = taskRepository.save(t);

        // Notify Employee if assigned
        if (saved.getEmployee() != null && (oldEmployee == null || !oldEmployee.getId().equals(saved.getEmployee().getId()))) {
            if (saved.getEmployee().getUser() != null) {
                notificationService.createNotification(
                        "New Task Assigned",
                        "You have been assigned task \"" + saved.getTitle() + "\" in project \"" + saved.getProject().getProjectName() + "\"",
                        saved.getEmployee().getUser(),
                        "TASK",
                        saved.getId()
                );
            }
        }

        return map(saved);
    }

    @Override
    public TaskDTO updateProgress(Long id, Integer progress) {
        if (progress == null || progress < 0 || progress > 100) {
            throw new IllegalArgumentException("Progress must be between 0 and 100");
        }
        Task t = find(id);
        validateTaskOwnership(t);
        
        String oldStatus = t.getStatus();
        t.setProgress(progress);
        
        if (progress == 100) {
            t.setStatus("COMPLETED");
            t.setCompletedDate(LocalDate.now());
            if (!"COMPLETED".equals(oldStatus)) {
                triggerCompletionNotification(t);
            }
        } else {
            if ("COMPLETED".equals(t.getStatus())) {
                t.setStatus("IN_PROGRESS");
                t.setCompletedDate(null);
            }
        }
        
        return map(taskRepository.save(t));
    }

    @Override
    public TaskDTO updateStatus(Long id, String status) {
        validateStatus(status);
        Task t = find(id);
        validateTaskOwnership(t);
        
        String oldStatus = t.getStatus();
        t.setStatus(status);
        
        if ("COMPLETED".equals(status)) {
            t.setProgress(100);
            t.setCompletedDate(LocalDate.now());
            if (!"COMPLETED".equals(oldStatus)) {
                triggerCompletionNotification(t);
            }
        } else {
            t.setCompletedDate(null);
            if (t.getProgress() == 100) {
                t.setProgress(0); // Reset progress if moving back from completed
            }
        }
        
        return map(taskRepository.save(t));
    }

    @Override
    public TaskDTO addRemarks(Long id, String remarks) {
        if (remarks == null || remarks.isBlank()) {
            throw new IllegalArgumentException("Remarks are required");
        }
        Task t = find(id);
        validateTaskOwnership(t);
        t.setRemarks(remarks);
        return map(taskRepository.save(t));
    }

    @Override
    @Transactional(readOnly = true)
    public TaskDTO getTask(Long id) {
        Task t = find(id);
        validateTaskReadAccess(t);
        return map(t);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskDTO> getAllTasks() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        if (user.getRole() == Role.ADMIN) {
            return taskRepository.findAll().stream().map(this::map).toList();
        } else {
            // Employee only gets their assigned tasks
            if (user.getEmployee() == null) {
                return List.of();
            }
            return taskRepository.findByEmployeeId(user.getEmployee().getId()).stream().map(this::map).toList();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskDTO> searchTasks(String status, LocalDate deadline) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Task> tasks;
        if (user.getRole() == Role.ADMIN) {
            tasks = taskRepository.findAll();
        } else {
            if (user.getEmployee() == null) {
                return List.of();
            }
            tasks = taskRepository.findByEmployeeId(user.getEmployee().getId());
        }

        return tasks.stream()
                .filter(t -> status == null || status.isBlank() || t.getStatus().equalsIgnoreCase(status))
                .filter(t -> deadline == null || deadline.equals(t.getDeadline()))
                .map(this::map)
                .toList();
    }

    private void assign(Task t, Long employeeId, Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));
        t.setProject(project);
        
        if (employeeId == null) {
            t.setEmployee(null);
            return;
        }
        
        Employee e = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));
        if (!e.getProjects().contains(project)) {
            throw new IllegalArgumentException("Employee must be assigned to the project before receiving a task");
        }
        t.setEmployee(e);
    }

    private Task find(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
    }

    private void validateStatus(String status) {
        if (status == null || !(status.equals("PENDING") || status.equals("IN_PROGRESS") || status.equals("COMPLETED"))) {
            throw new IllegalArgumentException("Status must be PENDING, IN_PROGRESS or COMPLETED");
        }
    }

    private void validateTaskOwnership(Task t) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        if (user.getRole() == Role.ADMIN) {
            return;
        }
        
        if (t.getEmployee() == null || t.getEmployee().getUser() == null || !t.getEmployee().getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You are not authorized to modify this task");
        }
    }

    private void validateTaskReadAccess(Task t) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        if (user.getRole() == Role.ADMIN) {
            return;
        }
        
        // Employee can read if assigned to task or if they belong to the task's project
        boolean isAssignedToTask = t.getEmployee() != null && t.getEmployee().getUser() != null && t.getEmployee().getUser().getId().equals(user.getId());
        boolean isMemberOfProject = t.getProject() != null && t.getProject().getEmployees().stream()
                .anyMatch(emp -> emp.getUser() != null && emp.getUser().getId().equals(user.getId()));
        
        if (!isAssignedToTask && !isMemberOfProject) {
            throw new AccessDeniedException("You do not have access to view this task");
        }
    }

    private void triggerCompletionNotification(Task task) {
        String empName = task.getEmployee() != null ? (task.getEmployee().getFirstName() + " " + task.getEmployee().getLastName()) : "An employee";
        String projName = task.getProject() != null ? task.getProject().getProjectName() : "Unknown Project";
        String title = "Task Completed";
        String message = empName + " completed \"" + task.getTitle() + "\" in project \"" + projName + "\".";

        // Notify all Admin users
        List<User> admins = userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.ADMIN)
                .toList();
        for (User admin : admins) {
            notificationService.createNotification(title, message, admin, "TASK", task.getId());
        }
    }

    private TaskDTO map(Task t) {
        return TaskDTO.builder()
                .id(t.getId())
                .title(t.getTitle())
                .description(t.getDescription())
                .status(t.getStatus())
                .progress(t.getProgress())
                .priority(t.getPriority())
                .remarks(t.getRemarks())
                .deadline(t.getDeadline())
                .assignedDate(t.getAssignedDate())
                .dueDate(t.getDueDate())
                .completedDate(t.getCompletedDate())
                .estimatedHours(t.getEstimatedHours())
                .employeeId(t.getEmployee() == null ? null : t.getEmployee().getId())
                .employeeName(t.getEmployee() != null ? (t.getEmployee().getFirstName() + " " + t.getEmployee().getLastName()) : null)
                .projectId(t.getProject() == null ? null : t.getProject().getId())
                .projectName(t.getProject() != null ? t.getProject().getProjectName() : null)
                .build();
    }
}
