package com.example.employeemanagement.service.impl;

import com.example.employeemanagement.dto.*;
import com.example.employeemanagement.exception.ResourceNotFoundException;
import com.example.employeemanagement.repository.*;
import com.example.employeemanagement.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;

@Service @RequiredArgsConstructor @Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {
    private static final String COMPLETED = "COMPLETED";
    private final EmployeeRepository employeeRepository; private final ProjectRepository projectRepository; private final TaskRepository taskRepository;
    public AdminDashboardDTO getAdminDashboard() { long totalTasks = taskRepository.count(); long completed = taskRepository.countByStatus(COMPLETED); return AdminDashboardDTO.builder().totalEmployees(employeeRepository.count()).totalProjects(projectRepository.count()).completedTasks(completed).pendingTasks(totalTasks - completed).build(); }
    public EmployeeDashboardDTO getEmployeeDashboard(Long employeeId) { if (!employeeRepository.existsById(employeeId)) throw new ResourceNotFoundException("Employee not found with id: " + employeeId); LocalDate today = LocalDate.now(); return EmployeeDashboardDTO.builder().assignedTasks(taskRepository.countByEmployeeId(employeeId)).completedTasks(taskRepository.countByEmployeeIdAndStatus(employeeId, COMPLETED)).upcomingDeadlines(taskRepository.findByDeadlineBetween(today, today.plusDays(7)).stream().filter(task -> task.getEmployee() != null && employeeId.equals(task.getEmployee().getId()) && !COMPLETED.equals(task.getStatus())).count()).build(); }
}
