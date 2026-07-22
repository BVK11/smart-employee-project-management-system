package com.example.employeemanagement.service;

import com.example.employeemanagement.dto.NotificationDTO;
import com.example.employeemanagement.entity.User;
import java.util.List;

public interface NotificationService {
    void createNotification(String title, String message, User recipient, String referenceType, Long referenceId);
    List<NotificationDTO> getMyNotifications(String email);
    List<NotificationDTO> getMyUnreadNotifications(String email);
    long getUnreadCount(String email);
    void markAsRead(Long id, String email);
    void markAllAsRead(String email);
}
