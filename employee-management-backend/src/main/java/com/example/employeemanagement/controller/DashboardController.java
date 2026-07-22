package com.example.employeemanagement.controller;
import com.example.employeemanagement.dto.*;
import com.example.employeemanagement.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/dashboard") @RequiredArgsConstructor
public class DashboardController {
    private final DashboardService dashboardService;
    @GetMapping("/admin") @PreAuthorize("hasRole('ADMIN')") public ResponseEntity<AdminDashboardDTO> admin() { return ResponseEntity.ok(dashboardService.getAdminDashboard()); }
    @GetMapping("/employee/{employeeId}") @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE')") public ResponseEntity<EmployeeDashboardDTO> employee(@PathVariable Long employeeId) { return ResponseEntity.ok(dashboardService.getEmployeeDashboard(employeeId)); }
}
