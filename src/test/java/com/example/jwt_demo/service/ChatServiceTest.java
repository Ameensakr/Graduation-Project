package com.example.jwt_demo.service;

import com.example.jwt_demo.dto.AIReply;
import com.example.jwt_demo.exception.AIServiceException;
import com.example.jwt_demo.model.ChatConversation;
import com.example.jwt_demo.model.ChatMessage;
import com.example.jwt_demo.repository.ConversationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock ConversationRepository conversationRepository;
    @Mock AIService aiService;
    @InjectMocks ChatService chatService;

    @BeforeEach
    void stubSave() {
        lenient().when(conversationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    private ChatMessage userMsg(String convId, String uid, String content) {
        return new ChatMessage(convId, uid, "user", content, "chat", null, LocalDateTime.now());
    }

    private ChatMessage botMsg(String convId, String content) {
        return new ChatMessage(convId, null, "bot", content, "chat", null, LocalDateTime.now());
    }

    @Test
    void sendMessage_newConversation_createsAndSetsTitle() {
        when(aiService.getReply(eq("hi"), any(), any(), any()))
                .thenReturn(new AIReply("chat", "hello!", null));
        when(aiService.generateTitle("hi")).thenReturn("Greeting");

        ChatConversation result = chatService.sendMessage("u1", "hi", null, null, "chat");

        assertThat(result.getUserId()).isEqualTo("u1");
        assertThat(result.getConversationId()).isNotBlank();
        assertThat(result.getTitle()).isEqualTo("Greeting");
        assertThat(result.getMessages()).hasSize(2);
        assertThat(result.getMessages().get(0).getSender()).isEqualTo("user");
        assertThat(result.getMessages().get(0).getContent()).isEqualTo("hi");
        assertThat(result.getMessages().get(0).getType()).isEqualTo("chat");
        assertThat(result.getMessages().get(1).getSender()).isEqualTo("bot");
        assertThat(result.getMessages().get(1).getContent()).isEqualTo("hello!");
        assertThat(result.getMessages().get(1).getType()).isEqualTo("chat");
    }

    @Test
    void sendMessage_planType_storesPlanData() {
        Map<String, Object> plan = Map.of("title", "Cairo Trip", "days", List.of());
        when(aiService.getReply(any(), any(), eq("plan"), any()))
                .thenReturn(new AIReply("plan", null, plan));
        when(aiService.generateTitle(any())).thenReturn("Cairo");

        ChatConversation result = chatService.sendMessage("u1", "plan a trip", null, null, "plan");

        ChatMessage bot = result.getMessages().get(1);
        assertThat(bot.getType()).isEqualTo("plan");
        assertThat(bot.getData()).isEqualTo(plan);
        assertThat(bot.getContent()).isNull();
    }

    @Test
    void sendMessage_newConversation_titleFallsBackToTruncatedMessage_whenAiReturnsNull() {
        when(aiService.getReply(any(), any(), any(), any()))
                .thenReturn(new AIReply("chat", "ok", null));
        when(aiService.generateTitle(any())).thenReturn(null);

        String longMsg = "a".repeat(40);
        ChatConversation result = chatService.sendMessage("u1", longMsg, "", null, "chat");

        assertThat(result.getTitle()).hasSize(33).endsWith("...");
    }

    @Test
    void sendMessage_existingConversation_appendsMessages_noTitleChange() {
        ChatConversation existing = new ChatConversation();
        existing.setConversationId("c1");
        existing.setUserId("u1");
        existing.setTitle("Existing Title");
        existing.setMessages(new ArrayList<>(List.of(userMsg("c1", "u1", "old"))));

        when(conversationRepository.findByConversationIdAndUserId("c1", "u1"))
                .thenReturn(Optional.of(existing));
        when(aiService.getReply(any(), any(), any(), any()))
                .thenReturn(new AIReply("chat", "reply", null));

        ChatConversation result = chatService.sendMessage("u1", "new", "c1", null, "chat");

        assertThat(result.getTitle()).isEqualTo("Existing Title");
        assertThat(result.getMessages()).hasSize(3);
        verify(aiService, never()).generateTitle(any());
    }

    @Test
    void sendMessage_existingConversation_notFound_throws404() {
        when(conversationRepository.findByConversationIdAndUserId("c1", "u1"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> chatService.sendMessage("u1", "hi", "c1", null, "chat"))
                .isInstanceOf(ResponseStatusException.class);
        verifyNoInteractions(aiService);
    }

    @Test
    void sendMessage_aiFails_savesConversationWithoutBotReply_andRethrows() {
        when(aiService.getReply(any(), any(), any(), any()))
                .thenThrow(new AIServiceException("down"));

        assertThatThrownBy(() -> chatService.sendMessage("u1", "hi", null, null, "chat"))
                .isInstanceOf(AIServiceException.class);

        ArgumentCaptor<ChatConversation> captor = ArgumentCaptor.forClass(ChatConversation.class);
        verify(conversationRepository).save(captor.capture());
        ChatConversation saved = captor.getValue();
        assertThat(saved.getMessages()).hasSize(1);
        assertThat(saved.getMessages().get(0).getSender()).isEqualTo("user");
    }

    @Test
    void regenerateLastReply_appendsNewBotMessage() {
        ChatConversation conv = new ChatConversation();
        conv.setConversationId("c1");
        conv.setUserId("u1");
        conv.setMessages(new ArrayList<>(List.of(userMsg("c1", "u1", "what time?"))));
        when(conversationRepository.findByConversationIdAndUserId("c1", "u1"))
                .thenReturn(Optional.of(conv));
        when(aiService.getReply(eq("what time?"), eq(null), any(), any()))
                .thenReturn(new AIReply("chat", "noon", null));

        ChatConversation updated = chatService.regenerateLastReply("c1", "u1");

        assertThat(updated.getMessages()).hasSize(2);
        assertThat(updated.getMessages().get(1).getSender()).isEqualTo("bot");
        assertThat(updated.getMessages().get(1).getContent()).isEqualTo("noon");
    }

    @Test
    void regenerateLastReply_emptyConversation_throws400() {
        ChatConversation conv = new ChatConversation();
        conv.setConversationId("c1");
        conv.setUserId("u1");
        conv.setMessages(new ArrayList<>());
        when(conversationRepository.findByConversationIdAndUserId("c1", "u1"))
                .thenReturn(Optional.of(conv));

        assertThatThrownBy(() -> chatService.regenerateLastReply("c1", "u1"))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void regenerateLastReply_lastMessageFromBot_throws400() {
        ChatConversation conv = new ChatConversation();
        conv.setConversationId("c1");
        conv.setUserId("u1");
        conv.setMessages(new ArrayList<>(List.of(botMsg("c1", "hi"))));
        when(conversationRepository.findByConversationIdAndUserId("c1", "u1"))
                .thenReturn(Optional.of(conv));

        assertThatThrownBy(() -> chatService.regenerateLastReply("c1", "u1"))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void regenerateLastReply_conversationMissing_throws404() {
        when(conversationRepository.findByConversationIdAndUserId("c1", "u1"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> chatService.regenerateLastReply("c1", "u1"))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void deleteConversation_existing_deletes() {
        ChatConversation conv = new ChatConversation();
        conv.setConversationId("c1");
        conv.setUserId("u1");
        when(conversationRepository.findByConversationIdAndUserId("c1", "u1"))
                .thenReturn(Optional.of(conv));

        chatService.deleteConversation("c1", "u1");

        verify(conversationRepository).delete(conv);
    }

    @Test
    void deleteConversation_missing_throws404() {
        when(conversationRepository.findByConversationIdAndUserId("c1", "u1"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> chatService.deleteConversation("c1", "u1"))
                .isInstanceOf(ResponseStatusException.class);
        verify(conversationRepository, never()).delete(any(ChatConversation.class));
    }

    @Test
    void getUserConversations_delegates() {
        ChatConversation c = new ChatConversation();
        when(conversationRepository.findByUserId("u1")).thenReturn(List.of(c));
        assertThat(chatService.getUserConversations("u1")).containsExactly(c);
    }

    @Test
    void getConversation_delegates() {
        ChatConversation c = new ChatConversation();
        when(conversationRepository.findByConversationIdAndUserId("c1", "u1"))
                .thenReturn(Optional.of(c));
        assertThat(chatService.getConversation("c1", "u1")).contains(c);
    }
}
