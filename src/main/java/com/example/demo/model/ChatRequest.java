package com.example.demo.model;

public class ChatRequest {
    private String message;      // 用户输入
    private String systemPrompt; // 系统提示（可选）
    private String sessionId;    // 会话 ID（多轮对话用）

    // 默认构造方法
    public ChatRequest() {
    }

    // 带参构造方法
    public ChatRequest(String message) {
        this.message = message;
    }

    public ChatRequest(String message, String systemPrompt, String sessionId) {
        this.message = message;
        this.systemPrompt = systemPrompt;
        this.sessionId = sessionId;
    }

    // Getter & Setter
    public String getMessage() { 
        return message; 
    }
    
    public void setMessage(String message) { 
        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("消息内容不能为空");
        }
        this.message = message; 
    }

    public String getSystemPrompt() { 
        return systemPrompt; 
    }
    
    public void setSystemPrompt(String systemPrompt) { 
        this.systemPrompt = systemPrompt; 
    }

    public String getSessionId() { 
        return sessionId; 
    }
    
    public void setSessionId(String sessionId) { 
        this.sessionId = sessionId; 
    }

    @Override
    public String toString() {
        return "ChatRequest{" +
                "message='" + message + '\'' +
                ", systemPrompt='" + systemPrompt + '\'' +
                ", sessionId='" + sessionId + '\'' +
                '}';
    }
}
