package com.example.demo.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@Service
public class ChatService {

    private final ChatClient chatClient;
    private final ChatContext chatContext;
    private final RoleService roleService;

    public ChatService(ChatClient.Builder chatClientBuilder, ChatContext chatContext, RoleService roleService) {
        this.chatClient = chatClientBuilder.build();
        this.chatContext = chatContext;
        this.roleService = roleService;
    }

    /** 简单对话（不带上下文） */
    public String simpleChat(String message) {
        return chatClient.prompt()
                .user(message)
                .call()
                .content();
    }

    /** 带系统提示的对话（不带上下文） */
    public String chatWithSystem(String systemPrompt, String message) {
        return chatClient.prompt()
                .system(systemPrompt)
                .user(message)
                .call()
                .content();
    }

    /** 流式对话 */
    public Flux<String> streamChat(String message) {
        return chatClient.prompt()
                .user(message)
                .stream()
                .content();
    }

    /** 多轮对话（普通回复） */
    public String chatWithHistory(String sessionId, String message, String systemPrompt) {
        return chatWithHistory(sessionId, message, systemPrompt, null);
    }

    /** 多轮对话（带角色） */
    public String chatWithRole(String sessionId, String message, String roleId) {
        String systemPrompt = roleService.getSystemPrompt(roleId);
        Map<String, Object> roleParams = roleService.getRoleParameters(roleId);
        return chatWithHistory(sessionId, message, systemPrompt, roleParams);
    }

    /** 多轮对话（完整参数） */
    private String chatWithHistory(String sessionId, String message, String systemPrompt, Map<String, Object> parameters) {
        if (systemPrompt != null && !systemPrompt.isBlank()) {
            chatContext.setSystemMessage(sessionId, systemPrompt);
        }

        chatContext.addUserMessage(sessionId, message);
        List<Message> historyMessages = chatContext.getMessages(sessionId);

        // ✅ 修复③：用变量接收 options() 的返回值，否则设置不生效
        var promptSpec = chatClient.prompt().messages(historyMessages);

        String response;
        if (parameters != null && !parameters.isEmpty()) {
            OpenAiChatOptions options = OpenAiChatOptions.builder()
                    .temperature((Double) parameters.getOrDefault("temperature", 0.7))
                    .maxTokens((Integer) parameters.getOrDefault("maxTokens", 2000))
                    .build();
            response = promptSpec.options(options).call().content();
        } else {
            response = promptSpec.call().content();
        }

        chatContext.addAssistantMessage(sessionId, response);
        return response;
    }

    /** 多轮对话（流式回复） */
    public Flux<String> streamChatWithHistory(String sessionId, String message, String systemPrompt) {
        return streamChatWithHistory(sessionId, message, systemPrompt, null);
    }

    /** 多轮对话（流式回复，带角色） */
    public Flux<String> streamChatWithRole(String sessionId, String message, String roleId) {
        String systemPrompt = roleService.getSystemPrompt(roleId);
        Map<String, Object> roleParams = roleService.getRoleParameters(roleId);
        return streamChatWithHistory(sessionId, message, systemPrompt, roleParams);
    }

    /** 多轮对话（流式回复，完整参数） */
    private Flux<String> streamChatWithHistory(String sessionId, String message, String systemPrompt, Map<String, Object> parameters) {
        if (systemPrompt != null && !systemPrompt.isBlank()) {
            chatContext.setSystemMessage(sessionId, systemPrompt);
        }

        chatContext.addUserMessage(sessionId, message);
        List<Message> historyMessages = chatContext.getMessages(sessionId);

        // ✅ 修复③：同上，链式调用结果要接收
        var promptSpec = chatClient.prompt().messages(historyMessages);

        Flux<String> responseFlux;
        if (parameters != null && !parameters.isEmpty()) {
            OpenAiChatOptions options = OpenAiChatOptions.builder()
                    .temperature((Double) parameters.getOrDefault("temperature", 0.7))
                    .maxTokens((Integer) parameters.getOrDefault("maxTokens", 2000))
                    .build();
            responseFlux = promptSpec.options(options).stream().content();
        } else {
            responseFlux = promptSpec.stream().content();
        }

        StringBuilder fullResponse = new StringBuilder();
        return responseFlux
                .doOnNext(fullResponse::append)
                .doOnComplete(() -> chatContext.addAssistantMessage(sessionId, fullResponse.toString()));
    }

    /** 清空会话历史 */
    public void clearHistory(String sessionId) {
        chatContext.clear(sessionId);
    }
}
