package com.example.employeemanagement.service;

import com.example.employeemanagement.dto.ChatMessageDTO;
import com.example.employeemanagement.dto.SendMessageRequest;
import com.example.employeemanagement.dto.TeamMemberDTO;
import java.util.List;

public interface ChatService {
    ChatMessageDTO sendMessage(Long projectId, SendMessageRequest request, String email);
    List<ChatMessageDTO> getChatHistory(Long projectId, String email);
    List<TeamMemberDTO> getTeamMembers(Long projectId, String email);
}
