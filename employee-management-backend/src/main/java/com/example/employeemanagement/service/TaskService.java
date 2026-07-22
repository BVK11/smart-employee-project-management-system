package com.example.employeemanagement.service;
import com.example.employeemanagement.dto.TaskDTO;
import java.util.List;
import java.time.LocalDate;
public interface TaskService {
    TaskDTO createTask(TaskDTO taskDTO);
    TaskDTO updateTask(Long id, TaskDTO taskDTO);
    void deleteTask(Long id);
    TaskDTO assignTask(Long id, Long employeeId, Long projectId);
    TaskDTO updateProgress(Long id, Integer progress);
    TaskDTO updateStatus(Long id, String status);
    TaskDTO addRemarks(Long id, String remarks);
    TaskDTO getTask(Long id);
    List<TaskDTO> getAllTasks();
    List<TaskDTO> searchTasks(String status, LocalDate deadline);
}
