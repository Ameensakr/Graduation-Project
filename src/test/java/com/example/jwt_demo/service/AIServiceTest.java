package com.example.jwt_demo.service;

import com.example.jwt_demo.dto.AIReply;
import com.example.jwt_demo.exception.AIServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class AIServiceTest {

    private RestTemplate restTemplate;
    private MockRestServiceServer server;
    private AIService aiService;

    @BeforeEach
    void setUp() {
        restTemplate = new RestTemplate();
        server = MockRestServiceServer.createServer(restTemplate);
        aiService = new AIService(restTemplate);
        ReflectionTestUtils.setField(aiService, "aiModelUrl", "http://ai.test");
    }

    @Test
    void getReply_chat_returnsAnswer() {
        server.expect(requestTo("http://ai.test/ask"))
                .andExpect(method(POST))
                .andRespond(withSuccess("{\"type\":\"chat\",\"answer\":\"Hello\"}", MediaType.APPLICATION_JSON));

        AIReply reply = aiService.getReply("hi", null, "chat", null);

        assertThat(reply.getType()).isEqualTo("chat");
        assertThat(reply.getContent()).isEqualTo("Hello");
        assertThat(reply.getData()).isNull();
        server.verify();
    }

    @Test
    void getReply_plan_returnsData() {
        String json = "{\"type\":\"plan\",\"data\":{\"title\":\"Cairo Trip\",\"days\":[]}}";
        server.expect(requestTo("http://ai.test/ask"))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        AIReply reply = aiService.getReply("plan a trip", null, "plan", null);

        assertThat(reply.getType()).isEqualTo("plan");
        assertThat(reply.getContent()).isNull();
        assertThat(reply.getData()).isInstanceOf(Map.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) reply.getData();
        assertThat(data).containsEntry("title", "Cairo Trip");
    }

    @Test
    void getReply_withFiles_sendsMultipart() {
        server.expect(requestTo("http://ai.test/ask"))
                .andExpect(method(POST))
                .andRespond(withSuccess("{\"type\":\"chat\",\"answer\":\"Read it\"}", MediaType.APPLICATION_JSON));

        MockMultipartFile file = new MockMultipartFile(
                "files", "a.txt", "text/plain", "data".getBytes());

        AIReply reply = aiService.getReply("summarize", List.of(file), "chat", null);

        assertThat(reply.getContent()).isEqualTo("Read it");
    }

    @Test
    void getReply_missingAnswerAndData_throws() {
        server.expect(requestTo("http://ai.test/ask"))
                .andRespond(withSuccess("{\"type\":\"chat\"}", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> aiService.getReply("hi", null, "chat", null))
                .isInstanceOf(AIServiceException.class);
    }

    @Test
    void getReply_errorField_throws() {
        server.expect(requestTo("http://ai.test/ask"))
                .andRespond(withSuccess("{\"type\":\"chat\",\"error\":\"bad\"}", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> aiService.getReply("hi", null, "chat", null))
                .isInstanceOf(AIServiceException.class)
                .hasMessageContaining("bad");
    }

    @Test
    void getReply_serverError_wrappedInAIServiceException() {
        server.expect(requestTo("http://ai.test/ask"))
                .andRespond(withServerError());

        assertThatThrownBy(() -> aiService.getReply("hi", null, "chat", null))
                .isInstanceOf(AIServiceException.class);
    }

    @Test
    void getReply_blankUrl_throwsConfig() {
        ReflectionTestUtils.setField(aiService, "aiModelUrl", "");

        assertThatThrownBy(() -> aiService.getReply("hi", null, "chat", null))
                .isInstanceOf(AIServiceException.class)
                .hasMessageContaining("not configured");
    }

    @Test
    void generateTitle_returnsTrimmedTitle() {
        server.expect(requestTo("http://ai.test/ask"))
                .andRespond(withSuccess("{\"answer\":\"  My Cool Title  \"}", MediaType.APPLICATION_JSON));

        assertThat(aiService.generateTitle("hello world")).isEqualTo("My Cool Title");
    }

    @Test
    void generateTitle_truncatesLongAnswer() {
        String longTitle = "x".repeat(100);
        server.expect(requestTo("http://ai.test/ask"))
                .andRespond(withSuccess("{\"answer\":\"" + longTitle + "\"}", MediaType.APPLICATION_JSON));

        String t = aiService.generateTitle("hi");
        assertThat(t).hasSize(50).endsWith("...");
    }

    @Test
    void generateTitle_onError_returnsNull() {
        server.expect(requestTo("http://ai.test/ask"))
                .andRespond(withServerError());

        assertThat(aiService.generateTitle("hi")).isNull();
    }
}
