package com.example.jwt_demo.dto;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AIReply {
    private String type;     // "chat" or "plan"
    private String content;  // for chat: the text. For plan: null (or short summary)
    private Object data;     // for plan: the plan JSON. For chat: null
}