package com.example.employeemanagement.controller;

import com.example.employeemanagement.dto.*;
import com.example.employeemanagement.entity.Role;
import com.example.employeemanagement.entity.User;
import com.example.employeemanagement.exception.ResourceNotFoundException;
import com.example.employeemanagement.repository.UserRepository;
import com.example.employeemanagement.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final UserRepository userRepository;

    @PostMapping("/projects")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProjectDTO> create(@Valid @RequestBody ProjectDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(projectService.createProject(dto));
    }

    @PutMapping("/projects/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProjectDTO> update(@PathVariable Long id, @Valid @RequestBody ProjectDTO dto) {
        return ResponseEntity.ok(projectService.updateProject(id, dto));
    }

    @DeleteMapping("/projects/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/projects/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE')")
    public ResponseEntity<ProjectDTO> get(@PathVariable Long id, Principal principal) {
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        ProjectDTO project = projectService.getProject(id);
        if (user.getRole() == Role.EMPLOYEE) {
            if (user.getEmployee() == null || !project.getEmployeeIds().contains(user.getEmployee().getId())) {
                throw new org.springframework.security.access.AccessDeniedException("You are not authorized to access this project");
            }
        }
        
        return ResponseEntity.ok(project);
    }

    @GetMapping("/projects")
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE')")
    public ResponseEntity<List<ProjectDTO>> all(Principal principal) {
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        if (user.getRole() == Role.ADMIN) {
            return ResponseEntity.ok(projectService.getAllProjects());
        } else {
            if (user.getEmployee() == null) {
                return ResponseEntity.ok(List.of());
            }
            List<ProjectDTO> myProjects = projectService.getAllProjects().stream()
                    .filter(p -> p.getEmployeeIds().contains(user.getEmployee().getId()))
                    .toList();
            return ResponseEntity.ok(myProjects);
        }
    }

    @GetMapping("/employee/projects")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<List<ProjectDTO>> getEmployeeProjects(Principal principal) {
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        if (user.getEmployee() == null) {
            return ResponseEntity.ok(List.of());
        }
        
        List<ProjectDTO> myProjects = projectService.getAllProjects().stream()
                .filter(p -> p.getEmployeeIds().contains(user.getEmployee().getId()))
                .toList();
        return ResponseEntity.ok(myProjects);
    }

    @GetMapping("/projects/search")
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE')")
    public ResponseEntity<List<ProjectDTO>> search(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String keyword,
            Principal principal
    ) {
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        List<ProjectDTO> searchResult = projectService.searchProjects(status, priority, keyword);
        
        if (user.getRole() == Role.ADMIN) {
            return ResponseEntity.ok(searchResult);
        } else {
            if (user.getEmployee() == null) {
                return ResponseEntity.ok(List.of());
            }
            List<ProjectDTO> myProjects = searchResult.stream()
                    .filter(p -> p.getEmployeeIds().contains(user.getEmployee().getId()))
                    .toList();
            return ResponseEntity.ok(myProjects);
        }
    }

    @PutMapping("/projects/{id}/employees")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProjectDTO> assignEmployees(@PathVariable Long id, @Valid @RequestBody EmployeeAssignmentRequest request) {
        return ResponseEntity.ok(projectService.assignEmployees(id, request.getEmployeeIds()));
    }
}
