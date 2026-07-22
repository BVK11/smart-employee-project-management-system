package com.example.employeemanagement.repository;

import com.example.employeemanagement.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByProjectIdOrderByTimestampAsc(Long projectId);

    long countByProjectId(Long projectId);
}
