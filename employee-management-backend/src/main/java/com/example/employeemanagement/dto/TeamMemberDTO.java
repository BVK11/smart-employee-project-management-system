package com.example.employeemanagement.dto;

import lombok.*;

/**
 * Summary of a team member shown in the Project Team panel.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamMemberDTO {
    private Long employeeId;
    private Long userId;
    private String firstName;
    private String lastName;
    private String email;
    private String department;
    private String designation;
    private String employeeCode;
}
