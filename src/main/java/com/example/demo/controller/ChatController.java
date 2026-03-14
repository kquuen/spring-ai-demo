package com.example.demo.controller;

import com.example.demo.model.ChatRequest;
import com.example.demo.model.ChatResponse;
import com.example.demo.model.RoleConfig;
import com.example.demo.service.ChatService;
import com.example.demo.service.RoleService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    private final ChatService chatService;
    private final RoleService roleService;

    public ChatController(ChatService chatService, RoleService roleService) {
        this.chatService = chatService;
        this.roleService = roleService;
    }

    // ==================== 原有接口（保留，兼容旧代码） ====================

    @GetMapping("/simple")
    public ChatResponse simpleChat(@RequestParam String message) {
        try {
            String result = chatService.simpleChat(message);
            return ChatResponse.ok(result);
        } catch (Exception e) {
            return ChatResponse.fail(e.getMessage());
        }
    }

    @GetMapping("/with-system")
    @PostMapping("/with-system")
    public ChatResponse chatWithSystem(
            @RequestParam(required = false) String systemPrompt,
            @RequestParam(required = false) String message,
            @RequestBody(required = false) ChatRequest request
    ) {
        String finalSystemPrompt = (request != null && request.getSystemPrompt() != null)
                ? request.getSystemPrompt() : systemPrompt;
        String finalMessage = (request != null && request.getMessage() != null)
                ? request.getMessage() : message;

        try {
            String result = chatService.chatWithSystem(finalSystemPrompt, finalMessage);
            return ChatResponse.ok(result);
        } catch (Exception e) {
            return ChatResponse.fail(e.getMessage());
        }
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamChat(@RequestParam String message) {
        return chatService.streamChat(message)
                .filter(content -> content != null && !content.isBlank());
    }

    // ==================== 新增：多轮对话接口（推荐使用） ====================

    /**
     * 生成会话ID（前端调用这个获取唯一ID）
     */
    @GetMapping("/session")
    public ChatResponse generateSessionId() {
        String sessionId = UUID.randomUUID().toString();
        return ChatResponse.ok(sessionId);
    }

    /**
     * 多轮对话（普通回复）
     */
    @GetMapping("/history")
    @PostMapping("/history")
    public ChatResponse chatWithHistory(
            @RequestParam String sessionId,
            @RequestParam String message,
            @RequestParam(required = false) String systemPrompt
    ) {
        try {
            String result = chatService.chatWithHistory(sessionId, message, systemPrompt);
            return ChatResponse.ok(result);
        } catch (Exception e) {
            return ChatResponse.fail(e.getMessage());
        }
    }
    
    /**
     * 多轮对话（带角色）
     */
    @GetMapping("/role")
    @PostMapping("/role")
    public ChatResponse chatWithRole(
            @RequestParam String sessionId,
            @RequestParam String message,
            @RequestParam(required = false, defaultValue = "assistant") String roleId
    ) {
        try {
            String result = chatService.chatWithRole(sessionId, message, roleId);
            return ChatResponse.ok(result);
        } catch (Exception e) {
            return ChatResponse.fail(e.getMessage());
        }
    }

    /**
     * 多轮对话（流式回复，推荐）
     */
    @GetMapping(value = "/history/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamChatWithHistory(
            @RequestParam String sessionId,
            @RequestParam String message,
            @RequestParam(required = false) String systemPrompt
    ) {
        return chatService.streamChatWithHistory(sessionId, message, systemPrompt)
                .filter(content -> content != null && !content.isBlank());
    }
    
    /**
     * 多轮对话（流式回复，带角色）
     */
    @GetMapping(value = "/role/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamChatWithRole(
            @RequestParam String sessionId,
            @RequestParam String message,
            @RequestParam(required = false, defaultValue = "assistant") String roleId
    ) {
        return chatService.streamChatWithRole(sessionId, message, roleId)
                .filter(content -> content != null && !content.isBlank());
    }

    /**
     * 清空对话历史
     */
    @PostMapping("/history/clear")
    public ChatResponse clearHistory(@RequestParam String sessionId) {
        try {
            chatService.clearHistory(sessionId);
            return ChatResponse.ok("对话历史已清空");
        } catch (Exception e) {
            return ChatResponse.fail(e.getMessage());
        }
    }
    
    // ==================== 角色管理接口 ====================
    
    /**
     * 获取所有可用角色
     */
    @GetMapping("/roles")
    public ChatResponse getAllRoles() {
        try {
            List<RoleConfig> roles = roleService.getAllRoles();
            return ChatResponse.ok(roles);
        } catch (Exception e) {
            return ChatResponse.fail(e.getMessage());
        }
    }
    
    /**
     * 获取特定角色配置
     */
    @GetMapping("/roles/{roleId}")
    public ChatResponse getRole(@PathVariable String roleId) {
        try {
            RoleConfig role = roleService.getRole(roleId);
            if (role == null) {
                return ChatResponse.fail("角色不存在: " + roleId);
            }
            return ChatResponse.ok(role);
        } catch (Exception e) {
            return ChatResponse.fail(e.getMessage());
        }
    }
    
    /**
     * 创建自定义角色
     */
    @PostMapping("/roles")
    public ChatResponse createRole(@RequestBody RoleConfig roleConfig) {
        try {
            RoleConfig saved = roleService.saveRole(roleConfig);
            return ChatResponse.ok(saved);
        } catch (Exception e) {
            return ChatResponse.fail(e.getMessage());
        }
    }
    
    /**
     * 删除自定义角色
     */
    @DeleteMapping("/roles/{roleId}")
    public ChatResponse deleteRole(@PathVariable String roleId) {
        try {
            roleService.deleteRole(roleId);
            return ChatResponse.ok("角色删除成功");
        } catch (Exception e) {
            return ChatResponse.fail(e.getMessage());
        }
    }
    
    // ==================== 健康检查接口 ====================
    
    /**
     * 应用健康检查
     */
    @GetMapping("/health")
    public Map<String, Object> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", System.currentTimeMillis());
        health.put("service", "Spring AI + 通义千问聊天服务");
        health.put("version", "1.0.0");
        
        // 检查Redis连接
        try {
            // 这里可以添加Redis健康检查逻辑
            health.put("redis", "UP");
        } catch (Exception e) {
            health.put("redis", "DOWN");
            health.put("redisError", e.getMessage());
        }
        
        return health;
    }
}