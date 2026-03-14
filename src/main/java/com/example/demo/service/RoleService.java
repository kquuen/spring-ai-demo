package com.example.demo.service;

import com.example.demo.model.RoleConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 角色管理服务
 * 管理不同的系统提示词角色配置
 */
@Service
public class RoleService {
    
    private static final Logger log = LoggerFactory.getLogger(RoleService.class);
    
    private final Map<String, RoleConfig> roleCache = new ConcurrentHashMap<>();
    
    public RoleService() {
        initDefaultRoles();
    }
    
    private void initDefaultRoles() {
        // 初始化默认角色
        saveRole(RoleConfig.createAssistant());
        saveRole(RoleConfig.createCoder());
        saveRole(RoleConfig.createTranslator());
        saveRole(RoleConfig.createWriter());
        
        log.info("初始化默认角色完成，共{}个角色", roleCache.size());
    }
    
    public RoleConfig getRole(String roleId) {
        RoleConfig role = roleCache.get(roleId);
        if (role == null) {
            // 如果找不到，返回默认助手角色
            role = roleCache.get("assistant");
        }
        return role;
    }
    
    public List<RoleConfig> getAllRoles() {
        List<RoleConfig> roles = new ArrayList<>(roleCache.values());
        // 按优先级排序
        roles.sort(Comparator.comparingInt(RoleConfig::getPriority));
        return roles;
    }
    
    public RoleConfig saveRole(RoleConfig role) {
        if (role.getRoleId() == null) {
            role.setRoleId(generateRoleId());
        }
        
        role.update();
        roleCache.put(role.getRoleId(), role);
        
        log.info("保存角色配置: roleId={}, roleName={}", role.getRoleId(), role.getRoleName());
        return role;
    }
    
    public RoleConfig createCustomRole(String roleName, String systemPrompt, String description, 
                                      Double temperature, Integer maxTokens) {
        RoleConfig role = new RoleConfig();
        role.setRoleId(generateRoleId());
        role.setRoleName(roleName);
        role.setSystemPrompt(systemPrompt);
        role.setDescription(description);
        role.setTemperature(temperature != null ? temperature : 0.7);
        role.setMaxTokens(maxTokens != null ? maxTokens : 2000);
        role.setCreatedBy("system");
        
        return saveRole(role);
    }
    
    public void deleteRole(String roleId) {
        // 不允许删除默认角色
        if (isDefaultRole(roleId)) {
            throw new IllegalArgumentException("不能删除系统默认角色: " + roleId);
        }
        
        roleCache.remove(roleId);
        log.info("删除角色: roleId={}", roleId);
    }
    
    public String getSystemPrompt(String roleId) {
        RoleConfig role = getRole(roleId);
        return role.getSystemPrompt();
    }
    
    public Map<String, Object> getRoleParameters(String roleId) {
        RoleConfig role = getRole(roleId);
        if (role == null) {
            return Collections.emptyMap();
        }
        
        Map<String, Object> params = new HashMap<>();
        params.put("temperature", role.getTemperature());
        params.put("maxTokens", role.getMaxTokens());
        
        if (role.getParameters() != null) {
            params.putAll(role.getParameters());
        }
        
        return params;
    }
    
    private boolean isDefaultRole(String roleId) {
        return roleId.equals("assistant") || 
               roleId.equals("coder") || 
               roleId.equals("translator") || 
               roleId.equals("writer");
    }
    
    private String generateRoleId() {
        return "role_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }
}