package com.example.employeemanagement.repository;

import com.example.employeemanagement.entity.Notification;
import com.example.employeemanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserOrderByTimestampDesc(User user);

    long countByUserAndIsReadFalse(User user);

    List<Notification> findByUserAndIsReadFalseOrderByTimestampDesc(User user);
}
