package com.example.employeemanagement.dto;
import lombok.*;
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class AdminDashboardDTO { private long totalEmployees; private long totalProjects; private long completedTasks; private long pendingTasks; }
