package com.example.employeemanagement.dto;
import lombok.*;
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class EmployeeDashboardDTO { private long assignedTasks; private long completedTasks; private long upcomingDeadlines; }
