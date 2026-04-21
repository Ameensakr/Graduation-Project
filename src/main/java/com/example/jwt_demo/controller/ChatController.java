package com.example.jwt_demo.controller;

import com.example.jwt_demo.model.ChatConversation;
import com.example.jwt_demo.model.ChatRequest;
import com.example.jwt_demo.model.User;
import com.example.jwt_demo.service.ChatService;
import com.example.jwt_demo.service.UserService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/chats")
public class ChatController {

    private final ChatService chatService;
    private final UserService userService;

    public ChatController(ChatService chatService, UserService userService) {
        this.chatService = chatService;
        this.userService = userService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ChatConversation> sendMessage(
            @AuthenticationPrincipal String email,
            @RequestPart("message") String message,
            @RequestPart(value = "conversationId", required = false) String conversationId,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) {

        User user = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        ChatConversation conversation = chatService.sendMessage(
                user.getId(),
                message,
                conversationId,
                file
        );

        return ResponseEntity.ok(conversation);
    }

    @GetMapping
    public ResponseEntity<List<ChatConversation>> getMyChats(
            @AuthenticationPrincipal String email
    ) {

        User user = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<ChatConversation> conversations =
                chatService.getUserConversations(user.getId());

        return ResponseEntity.ok(conversations);
    }


    @GetMapping("/{conversationId}")
    public ResponseEntity<ChatConversation> getConversation(
            @AuthenticationPrincipal String email,
            @PathVariable String conversationId
    ) {

        User user = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return chatService.getConversation(conversationId, user.getId())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

}
