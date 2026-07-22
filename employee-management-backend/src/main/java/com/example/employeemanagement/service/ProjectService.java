package com.example.employeemanagement.service;
import com.example.employeemanagement.dto.ProjectDTO;
import java.util.List;
import java.util.Set;
public interface ProjectService {
    ProjectDTO createProject(ProjectDTO projectDTO);
    ProjectDTO updateProject(Long id, ProjectDTO projectDTO);
    void deleteProject(Long id);
    ProjectDTO getProject(Long id);
    List<ProjectDTO> getAllProjects();
    List<ProjectDTO> searchProjects(String status, String priority, String keyword);
    ProjectDTO assignEmployees(Long id, Set<Long> employeeIds);
}
