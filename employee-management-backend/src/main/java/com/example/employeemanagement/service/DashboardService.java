package com.example.employeemanagement.service;
import com.example.employeemanagement.dto.*;
public interface DashboardService { AdminDashboardDTO getAdminDashboard(); EmployeeDashboardDTO getEmployeeDashboard(Long employeeId); }
