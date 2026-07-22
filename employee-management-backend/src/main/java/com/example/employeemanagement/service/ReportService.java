package com.example.employeemanagement.service;
import com.example.employeemanagement.dto.*;
import java.util.List;
public interface ReportService { List<EmployeeReportDTO> getEmployeeReport(); List<ProjectProgressReportDTO> getProjectProgressReport(); List<PendingTaskReportDTO> getPendingTaskReport(); byte[] exportPdf(); byte[] exportExcel(); }
