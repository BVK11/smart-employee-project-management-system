package com.example.employeemanagement.controller;
import com.example.employeemanagement.dto.*;
import com.example.employeemanagement.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/api/reports") @RequiredArgsConstructor
public class ReportController {
    private final ReportService reportService;
    @GetMapping("/employee") @PreAuthorize("hasRole('ADMIN')") public ResponseEntity<List<EmployeeReportDTO>> employee() { return ResponseEntity.ok(reportService.getEmployeeReport()); }
    @GetMapping("/projects") @PreAuthorize("hasRole('ADMIN')") public ResponseEntity<List<ProjectProgressReportDTO>> projects() { return ResponseEntity.ok(reportService.getProjectProgressReport()); }
    @GetMapping("/tasks/pending") @PreAuthorize("hasRole('ADMIN')") public ResponseEntity<List<PendingTaskReportDTO>> pendingTasks() { return ResponseEntity.ok(reportService.getPendingTaskReport()); }
    @GetMapping("/pdf") @PreAuthorize("hasRole('ADMIN')") public ResponseEntity<byte[]> pdf() { return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF).header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=report.pdf").body(reportService.exportPdf()); }
    @GetMapping("/excel") @PreAuthorize("hasRole('ADMIN')") public ResponseEntity<byte[]> excel() { return ResponseEntity.ok().contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")).header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=report.xlsx").body(reportService.exportExcel()); }
}
