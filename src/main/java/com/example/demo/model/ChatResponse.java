package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatResponse {
    private boolean success;  // 是否成功
    private String error;     // 错误信息
    private Object data;      // 响应数据（可以是字符串、列表、对象等）
    private String content;   // AI 回复内容

    public ChatResponse() {
    }

    // 成功时的构造（字符串内容，如AI回复）
    public static ChatResponse ok(String content) {
        ChatResponse r = new ChatResponse();
        r.content = content;
        r.success = true;
        return r;
    }

    // 成功时的构造（任意对象，如角色列表、角色配置等）
    public static ChatResponse ok(Object data) {
        ChatResponse r = new ChatResponse();
        r.data = data;
        r.success = true;
        return r;
    }

    // 失败时的构造
    public static ChatResponse fail(String error) {
        ChatResponse r = new ChatResponse();
        r.success = false;
        r.error = error;
        return r;
    }

    // Getter
    public boolean isSuccess() { return success; }
    public String getError() { return error; }
    public Object getData() { return data; }
    public String getContent() { return content; }

    // Setter (用于 JSON 反序列化)
    public void setSuccess(boolean success) { this.success = success; }
    public void setError(String error) { this.error = error; }
    public void setData(Object data) { this.data = data; }   // ✅ 修复：T -> Object
    public void setContent(String content) { this.content = content; }
}
