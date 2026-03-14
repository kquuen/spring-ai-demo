package com.example.demo.service;

import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 对话上下文管理器
 * 用 Session ID 区分不同用户的对话，实现多轮记忆
 */
@Component
public class ChatContext {

    private final Map<String, List<Message>> contextMap = new ConcurrentHashMap<>();

    public List<Message> getMessages(String sessionId) {
        return contextMap.computeIfAbsent(sessionId, k -> new ArrayList<>());
    }

    public void addUserMessage(String sessionId, String content) {
        getMessages(sessionId).add(new UserMessage(content));
    }

    public void addAssistantMessage(String sessionId, String content) {
        getMessages(sessionId).add(new AssistantMessage(content));
    }

    public void setSystemMessage(String sessionId, String systemPrompt) {
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(systemPrompt));
        contextMap.put(sessionId, messages);
    }

    public void clear(String sessionId) {
        contextMap.remove(sessionId);
    }
}