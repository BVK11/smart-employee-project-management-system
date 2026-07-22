package com.example.employeemanagement.service.impl;

import com.example.employeemanagement.dto.NotificationDTO;
import com.example.employeemanagement.entity.Notification;
import com.example.employeemanagement.entity.User;
import com.example.employeemanagement.exception.ResourceNotFoundException;
import com.example.employeemanagement.repository.NotificationRepository;
import com.example.employeemanagement.repository.UserRepository;
import com.example.employeemanagement.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Override
    public void createNotification(String title, String message, User recipient, String referenceType, Long referenceId) {
        Notification notification = Notification.builder()
                .title(title)
                .message(message)
                .timestamp(LocalDateTime.now())
                .isRead(false)
                .user(recipient)
                .referenceType(referenceType)
                .referenceId(referenceId)
                .build();
        notificationRepository.save(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationDTO> getMyNotifications(String email) {
        User user = findUser(email);
        return notificationRepository.findByUserOrderByTimestampDesc(user)
                .stream()
                .map(this::map)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationDTO> getMyUnreadNotifications(String email) {
        User user = findUser(email);
        return notificationRepository.findByUserAndIsReadFalseOrderByTimestampDesc(user)
                .stream()
                .map(this::map)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(String email) {
        User user = findUser(email);
        return notificationRepository.countByUserAndIsReadFalse(user);
    }

    @Override
    public void markAsRead(Long id, String email) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + id));
        if (!notification.getUser().getEmail().equals(email)) {
            throw new IllegalArgumentException("Unauthorized to modify this notification");
        }
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Override
    public void markAllAsRead(String email) {
        User user = findUser(email);
        List<Notification> unread = notificationRepository.findByUserAndIsReadFalseOrderByTimestampDesc(user);
        for (Notification n : unread) {
            n.setRead(true);
        }
        notificationRepository.saveAll(unread);
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    private NotificationDTO map(Notification n) {
        return NotificationDTO.builder()
                .id(n.getId())
                .title(n.getTitle())
                .message(n.getMessage())
                .timestamp(n.getTimestamp())
                .isRead(n.isRead())
                .userId(n.getUser() != null ? n.getUser().getId() : null)
                .referenceType(n.getReferenceType())
                .referenceId(n.getReferenceId())
                .build();
    }
}
