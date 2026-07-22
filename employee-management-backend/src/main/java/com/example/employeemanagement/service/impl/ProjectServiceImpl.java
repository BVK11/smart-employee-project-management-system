package com.example.employeemanagement.service.impl;

import com.example.employeemanagement.dto.ProjectDTO;
import com.example.employeemanagement.entity.*;
import com.example.employeemanagement.exception.ResourceNotFoundException;
import com.example.employeemanagement.repository.*;
import com.example.employeemanagement.service.ProjectService;
import com.example.employeemanagement.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final EmployeeRepository employeeRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Override
    public ProjectDTO createProject(ProjectDTO dto) {
        validateDates(dto);
        Project project = Project.builder()
                .projectName(dto.getProjectName())
                .description(dto.getDescription())
                .priority(dto.getPriority())
                .status(dto.getStatus())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .build();
        
        project = projectRepository.save(project);
        replaceEmployees(project, employees(dto.getEmployeeIds()));
        Project saved = projectRepository.save(project);

        // Notify Admins
        List<User> admins = userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.ADMIN)
                .toList();
        for (User admin : admins) {
            notificationService.createNotification(
                    "New Project Created",
                    "Project \"" + saved.getProjectName() + "\" has been created.",
                    admin,
                    "PROJECT",
                    saved.getId()
            );
        }

        return map(saved);
    }

    @Override
    public ProjectDTO updateProject(Long id, ProjectDTO dto) {
        validateDates(dto);
        Project p = find(id);
        
        String oldStatus = p.getStatus();
        
        p.setProjectName(dto.getProjectName());
        p.setDescription(dto.getDescription());
        p.setPriority(dto.getPriority());
        p.setStatus(dto.getStatus());
        p.setStartDate(dto.getStartDate());
        p.setEndDate(dto.getEndDate());

        if (dto.getEmployeeIds() != null) {
            replaceEmployees(p, employees(dto.getEmployeeIds()));
        }

        Project saved = projectRepository.save(p);

        // Trigger Project Completed notification if status changed to completed
        if ("COMPLETED".equals(saved.getStatus()) && !"COMPLETED".equals(oldStatus)) {
            List<User> admins = userRepository.findAll().stream()
                    .filter(u -> u.getRole() == Role.ADMIN)
                    .toList();
            for (User admin : admins) {
                notificationService.createNotification(
                        "Project Completed",
                        "Project \"" + saved.getProjectName() + "\" is completed.",
                        admin,
                        "PROJECT",
                        saved.getId()
                );
            }
        }

        return map(saved);
    }

    @Override
    public void deleteProject(Long id) {
        Project p = find(id);
        taskRepository.findByProjectId(id).forEach(t -> t.setProject(null));
        p.getEmployees().forEach(e -> e.getProjects().remove(p));
        p.getEmployees().clear();
        projectRepository.delete(p);
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectDTO getProject(Long id) {
        return map(find(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectDTO> getAllProjects() {
        return projectRepository.findAll().stream().map(this::map).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectDTO> searchProjects(String status, String priority, String keyword) {
        return projectRepository.findAll().stream()
                .filter(p -> status == null || status.isBlank() || p.getStatus().equalsIgnoreCase(status))
                .filter(p -> priority == null || priority.isBlank() || p.getPriority().equalsIgnoreCase(priority))
                .filter(p -> keyword == null || keyword.isBlank() || p.getProjectName().toLowerCase().contains(keyword.toLowerCase()))
                .map(this::map)
                .toList();
    }

    @Override
    public ProjectDTO assignEmployees(Long id, Set<Long> employeeIds) {
        Project p = find(id);
        replaceEmployees(p, employees(employeeIds));
        return map(projectRepository.save(p));
    }

    private Project find(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
    }

    private Set<Employee> employees(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new HashSet<>();
        }
        Set<Employee> result = new HashSet<>(employeeRepository.findAllById(ids));
        if (result.size() != ids.size()) {
            throw new ResourceNotFoundException("One or more employees were not found");
        }
        return result;
    }

    private void replaceEmployees(Project project, Set<Employee> next) {
        Set<Employee> current = new HashSet<>(project.getEmployees());
        
        // Employees assigned
        for (Employee emp : next) {
            if (!current.contains(emp)) {
                if (emp.getUser() != null) {
                    notificationService.createNotification(
                            "Project Assignment",
                            "You have been assigned to project \"" + project.getProjectName() + "\"",
                            emp.getUser(),
                            "PROJECT",
                            project.getId()
                    );
                }
            }
        }
        
        // Employees removed
        for (Employee emp : current) {
            if (!next.contains(emp)) {
                if (emp.getUser() != null) {
                    notificationService.createNotification(
                            "Project Removal",
                            "You have been removed from project \"" + project.getProjectName() + "\"",
                            emp.getUser(),
                            "PROJECT",
                            project.getId()
                    );
                }
            }
        }

        current.forEach(employee -> employee.getProjects().remove(project));
        project.setEmployees(new HashSet<>(next));
        next.forEach(employee -> employee.getProjects().add(project));
    }

    private void validateDates(ProjectDTO dto) {
        if (dto.getStartDate() != null && dto.getEndDate() != null && dto.getEndDate().isBefore(dto.getStartDate())) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }
    }

    private ProjectDTO map(Project p) {
        List<Task> tasks = taskRepository.findByProjectId(p.getId());
        long total = tasks.size();
        long completed = tasks.stream().filter(t -> "COMPLETED".equals(t.getStatus())).count();
        double progress = total == 0 ? 0.0 : Math.round((completed * 100.0 / total) * 100.0) / 100.0;

        return ProjectDTO.builder()
                .id(p.getId())
                .projectName(p.getProjectName())
                .description(p.getDescription())
                .priority(p.getPriority())
                .status(p.getStatus())
                .startDate(p.getStartDate())
                .endDate(p.getEndDate())
                .employeeIds(p.getEmployees().stream().map(Employee::getId).collect(java.util.stream.Collectors.toSet()))
                .progress(progress)
                .build();
    }
}
