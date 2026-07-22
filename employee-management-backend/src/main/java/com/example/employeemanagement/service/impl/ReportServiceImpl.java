package com.example.employeemanagement.service.impl;

import com.example.employeemanagement.dto.*;
import com.example.employeemanagement.entity.*;
import com.example.employeemanagement.repository.*;
import com.example.employeemanagement.service.ReportService;
import com.lowagie.text.Document;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.ByteArrayOutputStream;
import java.util.List;

@Service @RequiredArgsConstructor @Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {
    private static final String COMPLETED = "COMPLETED";
    private final EmployeeRepository employeeRepository; private final ProjectRepository projectRepository; private final TaskRepository taskRepository;
    public List<EmployeeReportDTO> getEmployeeReport() { return employeeRepository.findAll().stream().map(e -> { long total = taskRepository.countByEmployeeId(e.getId()); long completed = taskRepository.countByEmployeeIdAndStatus(e.getId(), COMPLETED); return EmployeeReportDTO.builder().employeeName(e.getFirstName() + " " + e.getLastName()).assignedTasks(total).completedTasks(completed).pendingTasks(total - completed).build(); }).toList(); }
    public List<ProjectProgressReportDTO> getProjectProgressReport() { return projectRepository.findAll().stream().map(p -> { long total = taskRepository.findByProjectId(p.getId()).size(); long completed = taskRepository.countByProjectIdAndStatus(p.getId(), COMPLETED); return ProjectProgressReportDTO.builder().projectName(p.getProjectName()).assignedEmployeesCount(p.getEmployees().size()).totalTasks(total).completedTasks(completed).remainingTasks(total - completed).progressPercentage(total == 0 ? 0 : Math.round((completed * 10000.0) / total) / 100.0).build(); }).toList(); }
    public List<PendingTaskReportDTO> getPendingTaskReport() { return taskRepository.findAll().stream().filter(t -> !COMPLETED.equals(t.getStatus())).map(t -> PendingTaskReportDTO.builder().employeeName(t.getEmployee() == null ? "Unassigned" : t.getEmployee().getFirstName() + " " + t.getEmployee().getLastName()).projectName(t.getProject() == null ? "Unassigned" : t.getProject().getProjectName()).deadline(t.getDeadline()).priority(t.getProject() == null ? null : t.getProject().getPriority()).status(t.getStatus()).build()).toList(); }
    public byte[] exportPdf() { try (ByteArrayOutputStream output = new ByteArrayOutputStream()) { Document document = new Document(); PdfWriter.getInstance(document, output); document.open(); document.add(new Paragraph("Enterprise Management Summary Report", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16))); document.add(new Paragraph("Employee Task Summary")); PdfPTable table = new PdfPTable(4); for (String h : List.of("Employee", "Assigned", "Completed", "Pending")) table.addCell(h); for (EmployeeReportDTO row : getEmployeeReport()) { table.addCell(row.getEmployeeName()); table.addCell(String.valueOf(row.getAssignedTasks())); table.addCell(String.valueOf(row.getCompletedTasks())); table.addCell(String.valueOf(row.getPendingTasks())); } document.add(table); document.close(); return output.toByteArray(); } catch (Exception ex) { throw new IllegalStateException("Unable to generate PDF report", ex); } }
    public byte[] exportExcel() { try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) { Sheet sheet = workbook.createSheet("Employee Report"); Row header = sheet.createRow(0); String[] headers = {"Employee", "Assigned Tasks", "Completed Tasks", "Pending Tasks"}; for (int i = 0; i < headers.length; i++) header.createCell(i).setCellValue(headers[i]); int index = 1; for (EmployeeReportDTO item : getEmployeeReport()) { Row row = sheet.createRow(index++); row.createCell(0).setCellValue(item.getEmployeeName()); row.createCell(1).setCellValue(item.getAssignedTasks()); row.createCell(2).setCellValue(item.getCompletedTasks()); row.createCell(3).setCellValue(item.getPendingTasks()); } for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i); workbook.write(output); return output.toByteArray(); } catch (Exception ex) { throw new IllegalStateException("Unable to generate Excel report", ex); } }
}
