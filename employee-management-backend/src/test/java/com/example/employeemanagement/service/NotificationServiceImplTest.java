package com.example.employeemanagement.service;

import com.example.employeemanagement.dto.NotificationDTO;
import com.example.employeemanagement.entity.Notification;
import com.example.employeemanagement.entity.Role;
import com.example.employeemanagement.entity.User;
import com.example.employeemanagement.exception.ResourceNotFoundException;
import com.example.employeemanagement.repository.NotificationRepository;
import com.example.employeemanagement.repository.UserRepository;
import com.example.employeemanagement.service.impl.NotificationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for NotificationServiceImpl.
 * All repositories are mocked — no database connection required.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationService Unit Tests")
class NotificationServiceImplTest {

    @Mock private NotificationRepository notificationRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private User sampleUser;
    private Notification sampleNotification;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .id(1L).name("John Doe").email("john@company.com").role(Role.EMPLOYEE)
                .build();

        sampleNotification = Notification.builder()
                .id(100L).title("Task Assigned")
                .message("You have been assigned task \"Write Tests\"")
                .timestamp(LocalDateTime.now())
                .isRead(false).user(sampleUser)
                .referenceType("TASK").referenceId(10L)
                .build();
    }

    // ──────────────────────── createNotification ─────────────────────────────

    @Test
    @DisplayName("createNotification() persists a notification with correct fields")
    void createNotification_savesNotificationWithCorrectFields() {
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);

        notificationService.createNotification(
                "Task Assigned",
                "You have a new task",
                sampleUser,
                "TASK",
                10L
        );

        verify(notificationRepository).save(captor.capture());
        Notification saved = captor.getValue();

        assertThat(saved.getTitle()).isEqualTo("Task Assigned");
        assertThat(saved.getMessage()).isEqualTo("You have a new task");
        assertThat(saved.getUser()).isEqualTo(sampleUser);
        assertThat(saved.getReferenceType()).isEqualTo("TASK");
        assertThat(saved.getReferenceId()).isEqualTo(10L);
        assertThat(saved.isRead()).isFalse();
        assertThat(saved.getTimestamp()).isNotNull();
    }

    // ──────────────────────── getMyNotifications ─────────────────────────────

    @Test
    @DisplayName("getMyNotifications() returns all notifications for the user ordered by timestamp desc")
    void getMyNotifications_returnsNotificationsForUser() {
        when(userRepository.findByEmail("john@company.com")).thenReturn(Optional.of(sampleUser));
        when(notificationRepository.findByUserOrderByTimestampDesc(sampleUser))
                .thenReturn(List.of(sampleNotification));

        List<NotificationDTO> result = notificationService.getMyNotifications("john@company.com");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Task Assigned");
        assertThat(result.get(0).isRead()).isFalse();
    }

    @Test
    @DisplayName("getMyNotifications() throws ResourceNotFoundException when user does not exist")
    void getMyNotifications_throwsException_whenUserNotFound() {
        when(userRepository.findByEmail("ghost@company.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.getMyNotifications("ghost@company.com"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with email: ghost@company.com");
    }

    // ─────────────────────────── getUnreadCount ───────────────────────────────

    @Test
    @DisplayName("getUnreadCount() returns the number of unread notifications for the user")
    void getUnreadCount_returnsCorrectCount() {
        when(userRepository.findByEmail("john@company.com")).thenReturn(Optional.of(sampleUser));
        when(notificationRepository.countByUserAndIsReadFalse(sampleUser)).thenReturn(3L);

        long count = notificationService.getUnreadCount("john@company.com");

        assertThat(count).isEqualTo(3L);
    }

    // ─────────────────────────── markAsRead ──────────────────────────────────

    @Test
    @DisplayName("markAsRead() sets isRead to true for the notification when user matches")
    void markAsRead_succeeds_whenUserOwnsNotification() {
        when(notificationRepository.findById(100L)).thenReturn(Optional.of(sampleNotification));

        notificationService.markAsRead(100L, "john@company.com");

        assertThat(sampleNotification.isRead()).isTrue();
        verify(notificationRepository).save(sampleNotification);
    }

    @Test
    @DisplayName("markAsRead() throws IllegalArgumentException when a different user tries to mark the notification")
    void markAsRead_throwsException_whenUserDoesNotOwnNotification() {
        when(notificationRepository.findById(100L)).thenReturn(Optional.of(sampleNotification));

        assertThatThrownBy(() -> notificationService.markAsRead(100L, "other@company.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unauthorized");

        verify(notificationRepository, never()).save(any());
    }

    @Test
    @DisplayName("markAsRead() throws ResourceNotFoundException when notification does not exist")
    void markAsRead_throwsException_whenNotificationNotFound() {
        when(notificationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.markAsRead(999L, "john@company.com"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Notification not found with id: 999");
    }

    // ────────────────────────── markAllAsRead ────────────────────────────────

    @Test
    @DisplayName("markAllAsRead() marks all unread notifications as read for the user")
    void markAllAsRead_marksAllUnreadNotifications() {
        Notification unread1 = Notification.builder()
                .id(1L).title("N1").message("msg1").timestamp(LocalDateTime.now())
                .isRead(false).user(sampleUser).build();
        Notification unread2 = Notification.builder()
                .id(2L).title("N2").message("msg2").timestamp(LocalDateTime.now())
                .isRead(false).user(sampleUser).build();

        when(userRepository.findByEmail("john@company.com")).thenReturn(Optional.of(sampleUser));
        when(notificationRepository.findByUserAndIsReadFalseOrderByTimestampDesc(sampleUser))
                .thenReturn(List.of(unread1, unread2));

        notificationService.markAllAsRead("john@company.com");

        assertThat(unread1.isRead()).isTrue();
        assertThat(unread2.isRead()).isTrue();
        verify(notificationRepository).saveAll(anyList());
    }
}
