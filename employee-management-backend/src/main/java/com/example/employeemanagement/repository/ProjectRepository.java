package com.example.employeemanagement.repository;

import com.example.employeemanagement.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findByStatus(String status);

    List<Project> findByPriority(String priority);

    List<Project> findByProjectNameContainingIgnoreCase(String keyword);

    long countByStatus(String status);

    List<Project> findByEmployees_Id(Long employeeId);

}
