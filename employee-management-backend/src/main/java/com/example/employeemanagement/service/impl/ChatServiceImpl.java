package com.example.employeemanagement.service.impl;

import com.example.employeemanagement.dto.ChatMessageDTO;
import com.example.employeemanagement.dto.SendMessageRequest;
import com.example.employeemanagement.dto.TeamMemberDTO;
import com.example.employeemanagement.entity.*;
import com.example.employeemanagement.exception.ResourceNotFoundException;
import com.example.employeemanagement.repository.*;
import com.example.employeemanagement.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatServiceImpl implements ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    @Override
    public ChatMessageDTO sendMessage(Long projectId, SendMessageRequest request, String email) {
        Project project = findProject(projectId);
        User sender = findUser(email);

        validateMembership(project, sender);

        ChatMessage message = ChatMessage.builder()
                .project(project)
                .sender(sender)
                .content(request.getContent())
                .timestamp(LocalDateTime.now())
                .build();

        return map(chatMessageRepository.save(message));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageDTO> getChatHistory(Long projectId, String email) {
        Project project = findProject(projectId);
        User user = findUser(email);

        validateMembership(project, user);

        return chatMessageRepository.findByProjectIdOrderByTimestampAsc(projectId)
                .stream()
                .map(this::map)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamMemberDTO> getTeamMembers(Long projectId, String email) {
        Project project = findProject(projectId);
        User user = findUser(email);

        validateMembership(project, user);

        return project.getEmployees().stream()
                .map(emp -> TeamMemberDTO.builder()
                        .employeeId(emp.getId())
                        .userId(emp.getUser() != null ? emp.getUser().getId() : null)
                        .firstName(emp.getFirstName())
                        .lastName(emp.getLastName())
                        .email(emp.getEmail())
                        .department(emp.getDepartment() != null ? emp.getDepartment().name() : null)
                        .designation(emp.getDesignation())
                        .employeeCode(emp.getEmployeeCode())
                        .build())
                .toList();
    }

    private void validateMembership(Project project, User user) {
        if (user.getRole() == Role.ADMIN) {
            return; // Admin can access all projects
        }
        // Employee must be assigned to the project
        boolean isMember = project.getEmployees().stream()
                .anyMatch(emp -> emp.getUser() != null && emp.getUser().getId().equals(user.getId()));
        if (!isMember) {
            throw new AccessDeniedException("You are not a member of this project and cannot access its chat/team details");
        }
    }

    private Project findProject(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    private ChatMessageDTO map(ChatMessage m) {
        return ChatMessageDTO.builder()
                .id(m.getId())
                .projectId(m.getProject().getId())
                .senderId(m.getSender().getId())
                .senderName(m.getSender().getName())
                .senderEmail(m.getSender().getEmail())
                .content(m.getContent())
                .timestamp(m.getTimestamp())
                .build();
    }
}
