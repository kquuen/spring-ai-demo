package com.example.demo.model;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 角色配置实体
 * 用于管理不同的系统提示词角色
 */
public class RoleConfig {
    
    private String roleId;
    private String roleName;
    private String systemPrompt;
    private String description;
    private Map<String, Object> parameters;
    private Double temperature = 0.7;
    private Integer maxTokens = 2000;
    private Boolean enabled = true;
    private Integer priority = 1;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    
    public RoleConfig() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public void update() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getter 和 Setter 方法
    public String getRoleId() { return roleId; }
    public void setRoleId(String roleId) { this.roleId = roleId; }
    
    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }
    
    public String getSystemPrompt() { return systemPrompt; }
    public void setSystemPrompt(String systemPrompt) { this.systemPrompt = systemPrompt; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public Map<String, Object> getParameters() { return parameters; }
    public void setParameters(Map<String, Object> parameters) { this.parameters = parameters; }
    
    public Double getTemperature() { return temperature; }
    public void setTemperature(Double temperature) { this.temperature = temperature; }
    
    public Integer getMaxTokens() { return maxTokens; }
    public void setMaxTokens(Integer maxTokens) { this.maxTokens = maxTokens; }
    
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    
    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    
    // 预定义角色
    public static RoleConfig createAssistant() {
        RoleConfig role = new RoleConfig();
        role.roleId = "assistant";
        role.roleName = "智能助手";
        role.systemPrompt = "你是一个有用的AI助手，请用中文回答用户的问题。回答要简洁、准确、有帮助。";
        role.description = "通用AI助手，适用于各种问答场景";
        return role;
    }
    
    public static RoleConfig createCoder() {
        RoleConfig role = new RoleConfig();
        role.roleId = "coder";
        role.roleName = "代码专家";
        role.systemPrompt = "你是一个专业的程序员助手，擅长Java、Spring Boot、Python、JavaScript等技术。请提供高质量的代码示例、调试建议和最佳实践。";
        role.description = "编程和代码相关问题的专家";
        role.temperature = 0.3;
        return role;
    }
    
    public static RoleConfig createTranslator() {
        RoleConfig role = new RoleConfig();
        role.roleId = "translator";
        role.roleName = "翻译专家";
        role.systemPrompt = "你是一个专业的翻译助手，精通中英文互译。请提供准确、自然的翻译，保持原文风格和语气。";
        role.description = "中英文翻译专家";
        role.temperature = 0.5;
        return role;
    }
    
    public static RoleConfig createWriter() {
        RoleConfig role = new RoleConfig();
        role.roleId = "writer";
        role.roleName = "创意写手";
        role.systemPrompt = "你是一个创意写作助手，擅长故事创作、文案撰写、诗歌创作等。请发挥创意，提供有趣、有深度的内容。";
        role.description = "创意写作和内容创作";
        role.temperature = 0.9;
        return role;
    }
}