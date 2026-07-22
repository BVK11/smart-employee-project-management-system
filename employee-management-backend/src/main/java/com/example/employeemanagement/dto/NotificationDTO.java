package com.example.employeemanagement.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDTO {
    private Long id;
    private String title;
    private String message;
    private LocalDateTime timestamp;
    private boolean isRead;
    private Long userId;
    private String referenceType;
    private Long referenceId;
}
