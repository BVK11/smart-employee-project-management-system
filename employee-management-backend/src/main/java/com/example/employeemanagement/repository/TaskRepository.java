package com.example.employeemanagement.repository;

import com.example.employeemanagement.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByStatus(String status);

    List<Task> findByEmployeeId(Long employeeId);

    List<Task> findByProjectId(Long projectId);

    List<Task> findByDeadlineBefore(LocalDate deadline);

    List<Task> findByDeadline(LocalDate deadline);

    List<Task> findByDeadlineBetween(LocalDate startDate, LocalDate endDate);

    long countByStatus(String status);

    long countByEmployeeId(Long employeeId);

    long countByEmployeeIdAndStatus(Long employeeId, String status);

    long countByProjectIdAndStatus(Long projectId, String status);

}
