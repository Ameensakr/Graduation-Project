package com.example.jwt_demo.controller;

import com.example.jwt_demo.model.ChatConversation;
import com.example.jwt_demo.model.ChatRequest;
import com.example.jwt_demo.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    // إرسال رسالة + رد AI
    @PostMapping("/send")
    public ResponseEntity<ChatConversation> sendMessage(@RequestBody ChatRequest chatRequest) {
        ChatConversation conversation = chatService.sendMessage(
                chatRequest.getUserId(),
                chatRequest.getMessage(),
                chatRequest.getSender()
        );
        return ResponseEntity.ok(conversation);
    }


    // جميع محادثات المستخدم
    @GetMapping("/all")
    public ResponseEntity<List<ChatConversation>> getUserConversations(@RequestParam String userId) {
        return ResponseEntity.ok(chatService.getUserConversations(userId));
    }

    // محادثة واحدة
    @GetMapping("/conversation")
    public ResponseEntity<?> getConversation(@RequestParam String conversationId, @RequestParam String userId) {
        Optional<ChatConversation> conversation = chatService.getConversation(conversationId, userId);
        return conversation
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).body("Conversation not found"));
    }
}
