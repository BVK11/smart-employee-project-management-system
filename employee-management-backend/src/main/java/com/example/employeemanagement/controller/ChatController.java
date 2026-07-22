package com.example.employeemanagement.controller;

import com.example.employeemanagement.dto.ChatMessageDTO;
import com.example.employeemanagement.dto.SendMessageRequest;
import com.example.employeemanagement.dto.TeamMemberDTO;
import com.example.employeemanagement.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @GetMapping("/chat")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<List<ChatMessageDTO>> getChatHistory(
            @PathVariable Long projectId,
            Principal principal
    ) {
        return ResponseEntity.ok(chatService.getChatHistory(projectId, principal.getName()));
    }

    @PostMapping("/chat")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<ChatMessageDTO> sendMessage(
            @PathVariable Long projectId,
            @Valid @RequestBody SendMessageRequest request,
            Principal principal
    ) {
        return ResponseEntity.ok(chatService.sendMessage(projectId, request, principal.getName()));
    }

    @GetMapping("/team")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<List<TeamMemberDTO>> getTeamMembers(
            @PathVariable Long projectId,
            Principal principal
    ) {
        return ResponseEntity.ok(chatService.getTeamMembers(projectId, principal.getName()));
    }
}
