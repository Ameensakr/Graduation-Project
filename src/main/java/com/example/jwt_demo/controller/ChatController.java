package com.example.jwt_demo.controller;

import com.example.jwt_demo.model.ChatConversation;
import com.example.jwt_demo.model.User;
import com.example.jwt_demo.service.ChatService;
import com.example.jwt_demo.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

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
            @RequestPart(value = "message", required = false) String message,
            @RequestPart(value = "conversationId", required = false) String conversationId,
            @RequestPart(value = "type", required = false) String type,
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) {
        boolean noMessage = message == null || message.isBlank();
        boolean noFiles = files == null || files.isEmpty();
        if (noMessage && noFiles) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Request must contain a message or at least one file");
        }
        if (files != null && files.size() > 3) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Maximum 3 files per request");
        }

        User user = userService.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        ChatConversation conversation = chatService.sendMessage(
                user.getId(),
                message,
                conversationId,
                files,
                type
        );

        return ResponseEntity.ok(conversation);
    }

    @GetMapping
    public ResponseEntity<List<ChatConversation>> getMyChats(
            @AuthenticationPrincipal String email
    ) {

        User user = userService.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        List<ChatConversation> conversations =
                chatService.getUserConversations(user.getId());

        return ResponseEntity.ok(conversations);
    }

    @PostMapping("/{conversationId}/regenerate")
    public ResponseEntity<ChatConversation> regenerate(
            @AuthenticationPrincipal String email,
            @PathVariable String conversationId
    ) {
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        ChatConversation updated = chatService.regenerateLastReply(conversationId, user.getId());
        return ResponseEntity.ok(updated);
    }


    @DeleteMapping("/{conversationId}")
    public ResponseEntity<Void> deleteConversation(
            @AuthenticationPrincipal String email,
            @PathVariable String conversationId
    ) {
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        chatService.deleteConversation(conversationId, user.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{conversationId}")
    public ResponseEntity<ChatConversation> getConversation(
            @AuthenticationPrincipal String email,
            @PathVariable String conversationId
    ) {

        User user = userService.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        return chatService.getConversation(conversationId, user.getId())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

}
