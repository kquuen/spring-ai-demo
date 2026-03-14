package com.example.demo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Redis持久化的对话上下文管理器
 */
@Component
public class RedisChatContext {

    // ✅ 修复①：只保留一次字段声明
    private final RedisTemplate<String, Object> redisTemplate;

    // ✅ 修复②：手动声明 Logger，解决 log.debug() 找不到符号
    private static final Logger log = LoggerFactory.getLogger(RedisChatContext.class);

    private static final String SESSION_KEY_PREFIX = "chat:session:";
    private static final long SESSION_TTL_HOURS = 2;

    public RedisChatContext(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public List<Message> getMessages(String sessionId) {
        String key = SESSION_KEY_PREFIX + sessionId;
        @SuppressWarnings("unchecked")
        List<Message> messages = (List<Message>) redisTemplate.opsForValue().get(key);

        if (messages == null) {
            messages = new ArrayList<>();
        }

        redisTemplate.expire(key, SESSION_TTL_HOURS, TimeUnit.HOURS);
        return messages;
    }

    public void addUserMessage(String sessionId, String content) {
        List<Message> messages = getMessages(sessionId);
        messages.add(new UserMessage(content));
        saveMessages(sessionId, messages);
        log.debug("添加用户消息: sessionId={}, contentLength={}", sessionId, content.length());
    }

    public void addAssistantMessage(String sessionId, String content) {
        List<Message> messages = getMessages(sessionId);
        messages.add(new AssistantMessage(content));
        saveMessages(sessionId, messages);
        log.debug("添加助手消息: sessionId={}, contentLength={}", sessionId, content.length());
    }

    public void setSystemMessage(String sessionId, String systemPrompt) {
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(systemPrompt));
        saveMessages(sessionId, messages);
        log.debug("设置系统消息: sessionId={}, promptLength={}", sessionId, systemPrompt.length());
    }

    public void clear(String sessionId) {
        String key = SESSION_KEY_PREFIX + sessionId;
        redisTemplate.delete(key);
        log.debug("清空会话: sessionId={}", sessionId);
    }

    public SessionInfo getSessionInfo(String sessionId) {
        List<Message> messages = getMessages(sessionId);
        SessionInfo info = new SessionInfo();
        info.setSessionId(sessionId);
        info.setMessageCount(messages.size());
        info.setLastActive(LocalDateTime.now());

        int tokenEstimate = messages.stream()
                .mapToInt(msg -> estimateTokens(msg.getText()))
                .sum();
        info.setTokenEstimate(tokenEstimate);

        return info;
    }

    public List<SessionInfo> getAllSessions() {
        return new ArrayList<>();
    }

    private void saveMessages(String sessionId, List<Message> messages) {
        String key = SESSION_KEY_PREFIX + sessionId;
        redisTemplate.opsForValue().set(key, messages, SESSION_TTL_HOURS, TimeUnit.HOURS);
    }

    private int estimateTokens(String text) {
        if (text == null || text.isEmpty()) return 0;

        int chineseCount = 0;
        for (char c : text.toCharArray()) {
            if (Character.UnicodeScript.of(c) == Character.UnicodeScript.HAN) {
                chineseCount++;
            }
        }

        String[] englishWords = text.replaceAll("[^a-zA-Z\\s]", "").split("\\s+");
        int englishCount = 0;
        for (String word : englishWords) {
            if (!word.isEmpty()) {
                englishCount += (int) Math.ceil(word.length() / 4.0);
            }
        }

        return chineseCount + englishCount + (int) (text.length() * 0.2);
    }

    // ✅ 修复：去掉 @lombok.Data，改用手写 getter/setter（避免Lombok配置问题）
    public static class SessionInfo {
        private String sessionId;
        private int messageCount;
        private int tokenEstimate;
        private LocalDateTime lastActive;

        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }

        public int getMessageCount() { return messageCount; }
        public void setMessageCount(int messageCount) { this.messageCount = messageCount; }

        public int getTokenEstimate() { return tokenEstimate; }
        public void setTokenEstimate(int tokenEstimate) { this.tokenEstimate = tokenEstimate; }

        public LocalDateTime getLastActive() { return lastActive; }
        public void setLastActive(LocalDateTime lastActive) { this.lastActive = lastActive; }
    }
}
