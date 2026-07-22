package com.example.employeemanagement.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String description;

    private String status;

    private Integer progress;

    private String priority;

    private String remarks;

    private LocalDate deadline; // Keep deadline as alias or map to dueDate

    private LocalDate assignedDate;

    private LocalDate dueDate;

    private LocalDate completedDate;

    private Integer estimatedHours;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;
}