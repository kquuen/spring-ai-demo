// Spring AI 通义千问聊天助手 - 前端逻辑
class ChatApp {
    constructor() {
        this.apiBaseUrl = 'http://localhost:8080/api/chat';
        this.currentSessionId = null;
        this.currentRole = 'assistant';
        this.isStreaming = false;
        this.eventSource = null;
        
        // 初始化
        this.initElements();
        this.initEventListeners();
        this.loadRoles();
        this.createNewSession();
        this.checkBackendStatus();
    }
    
    initElements() {
        // 获取DOM元素
        this.elements = {
            messageInput: document.getElementById('message-input'),
            sendBtn: document.getElementById('send-btn'),
            stopBtn: document.getElementById('stop-btn'),
            messagesContainer: document.getElementById('messages-container'),
            currentSession: document.getElementById('current-session'),
            newSessionBtn: document.getElementById('new-session'),
            clearChatBtn: document.getElementById('clear-chat'),
            exportChatBtn: document.getElementById('export-chat'),
            rolesList: document.getElementById('roles-list'),
            streamToggle: document.getElementById('stream-toggle'),
            typingEffect: document.getElementById('typing-effect'),
            temperature: document.getElementById('temperature'),
            tempValue: document.getElementById('temp-value'),
            redisStatus: document.getElementById('redis-status'),
            modelStatus: document.getElementById('model-status'),
            loadingOverlay: document.getElementById('loading')
        };
        
        // 自动调整文本域高度
        this.elements.messageInput.addEventListener('input', () => {
            this.adjustTextareaHeight();
        });
    }
    
    initEventListeners() {
        // 发送消息
        this.elements.sendBtn.addEventListener('click', () => this.sendMessage());
        
        // 输入框回车发送
        this.elements.messageInput.addEventListener('keydown', (e) => {
            if (e.key === 'Enter' && !e.shiftKey) {
                e.preventDefault();
                this.sendMessage();
            }
        });
        
        // 停止流式响应
        this.elements.stopBtn.addEventListener('click', () => this.stopStreaming());
        
        // 新会话
        this.elements.newSessionBtn.addEventListener('click', () => this.createNewSession());
        
        // 清空对话
        this.elements.clearChatBtn.addEventListener('click', () => this.clearChat());
        
        // 导出对话
        this.elements.exportChatBtn.addEventListener('click', () => this.exportChat());
        
        // 温度滑块
        this.elements.temperature.addEventListener('input', (e) => {
            this.elements.tempValue.textContent = e.target.value;
        });
    }
    
    adjustTextareaHeight() {
        const textarea = this.elements.messageInput;
        textarea.style.height = 'auto';
        textarea.style.height = Math.min(textarea.scrollHeight, 200) + 'px';
    }
    
    async loadRoles() {
        try {
            const response = await fetch(`${this.apiBaseUrl}/roles`);
            const result = await response.json();
            
            if (result.success) {
                this.renderRoles(result.content);
            } else {
                console.error('加载角色失败:', result.message);
                this.renderDefaultRoles();
            }
        } catch (error) {
            console.error('加载角色时出错:', error);
            this.renderDefaultRoles();
        }
    }
    
    renderRoles(roles) {
        const rolesList = this.elements.rolesList;
        rolesList.innerHTML = '';
        
        roles.forEach(role => {
            const roleElement = document.createElement('div');
            roleElement.className = `role-item ${role.roleId === this.currentRole ? 'active' : ''}`;
            roleElement.innerHTML = `
                <div class="role-icon">
                    <i class="fas ${this.getRoleIcon(role.roleId)}"></i>
                </div>
                <div class="role-name">${role.roleName}</div>
                <div class="role-desc">${role.description || '无描述'}</div>
            `;
            
            roleElement.addEventListener('click', () => this.selectRole(role.roleId));
            rolesList.appendChild(roleElement);
        });
    }
    
    renderDefaultRoles() {
        const defaultRoles = [
            {
                roleId: 'assistant',
                roleName: '智能助手',
                description: '通用AI助手，适用于各种问答场景'
            },
            {
                roleId: 'coder',
                roleName: '代码专家',
                description: '编程和代码相关问题的专家'
            },
            {
                roleId: 'translator',
                roleName: '翻译专家',
                description: '中英文翻译专家'
            },
            {
                roleId: 'writer',
                roleName: '创意写手',
                description: '创意写作和内容创作'
            }
        ];
        
        this.renderRoles(defaultRoles);
    }
    
    getRoleIcon(roleId) {
        const icons = {
            'assistant': 'fa-robot',
            'coder': 'fa-code',
            'translator': 'fa-language',
            'writer': 'fa-pen-nib'
        };
        return icons[roleId] || 'fa-user';
    }
    
    selectRole(roleId) {
        this.currentRole = roleId;
        
        // 更新UI
        document.querySelectorAll('.role-item').forEach(item => {
            item.classList.remove('active');
        });
        
        const selectedItem = Array.from(document.querySelectorAll('.role-item'))
            .find(item => item.querySelector('.role-name').textContent === 
                this.getRoleName(roleId));
        
        if (selectedItem) {
            selectedItem.classList.add('active');
        }
        
        // 显示角色切换提示
        this.addSystemMessage(`已切换到「${this.getRoleName(roleId)}」角色`);
    }
    
    getRoleName(roleId) {
        const names = {
            'assistant': '智能助手',
            'coder': '代码专家',
            'translator': '翻译专家',
            'writer': '创意写手'
        };
        return names[roleId] || '未知角色';
    }
    
    async createNewSession() {
        try {
            const response = await fetch(`${this.apiBaseUrl}/session`);
            const result = await response.json();
            
            if (result.success) {
                this.currentSessionId = result.content;
                this.elements.currentSession.textContent = this.currentSessionId.substring(0, 16) + '...';
                
                // 清空消息容器（保留欢迎消息）
                const welcomeMessage = this.elements.messagesContainer.querySelector('.welcome-message');
                this.elements.messagesContainer.innerHTML = '';
                if (welcomeMessage) {
                    this.elements.messagesContainer.appendChild(welcomeMessage);
                }
                
                this.addSystemMessage('新会话已创建，可以开始对话了！');
            }
        } catch (error) {
            console.error('创建会话失败:', error);
            // 如果API失败，生成本地会话ID
            this.currentSessionId = 'sess_' + Math.random().toString(36).substr(2, 16);
            this.elements.currentSession.textContent = this.currentSessionId.substring(0, 16) + '...';
            this.addSystemMessage('新会话已创建（本地模式）');
        }
    }
    
    async sendMessage() {
        const message = this.elements.messageInput.value.trim();
        if (!message || this.isStreaming) return;
        
        // 添加用户消息到UI
        this.addUserMessage(message);
        
        // 清空输入框
        this.elements.messageInput.value = '';
        this.adjustTextareaHeight();
        
        // 显示加载状态
        this.showLoading(true);
        
        // 根据设置选择发送方式
        const useStream = this.elements.streamToggle.checked;
        
        if (useStream) {
            await this.sendStreamMessage(message);
        } else {
            await this.sendNormalMessage(message);
        }
        
        this.showLoading(false);
    }
    
    async sendNormalMessage(message) {
        try {
            const url = `${this.apiBaseUrl}/role?sessionId=${this.currentSessionId}&message=${encodeURIComponent(message)}&roleId=${this.currentRole}`;
            const response = await fetch(url, {
                method: 'POST'
            });
            
            const result = await response.json();
            
            if (result.success) {
                this.addAssistantMessage(result.content);
            } else {
                this.addErrorMessage(result.message || '请求失败');
            }
        } catch (error) {
            console.error('发送消息失败:', error);
            this.addErrorMessage('网络错误，请检查后端服务');
        }
    }
    
    async sendStreamMessage(message) {
        this.isStreaming = true;
        this.elements.stopBtn.style.display = 'flex';
        this.elements.sendBtn.style.display = 'none';
        
        const useTypingEffect = this.elements.typingEffect.checked;
        let assistantMessageId = null;
        let fullResponse = '';
        
        try {
            const url = `${this.apiBaseUrl}/role/stream?sessionId=${this.currentSessionId}&message=${encodeURIComponent(message)}&roleId=${this.currentRole}`;
            
            this.eventSource = new EventSource(url);
            
            this.eventSource.onmessage = (event) => {
                const data = event.data;
                
                if (!assistantMessageId) {
                    // 创建新的助手消息容器
                    assistantMessageId = this.createAssistantMessageContainer();
                }
                
                if (useTypingEffect) {
                    this.appendToMessageWithTyping(assistantMessageId, data);
                } else {
                    this.appendToMessage(assistantMessageId, data);
                }
                
                fullResponse += data;
                
                // 自动滚动到底部
                this.scrollToBottom();
            };
            
            this.eventSource.onerror = (error) => {
                this.eventSource.close();
                this.isStreaming = false;
                this.elements.stopBtn.style.display = 'none';
                this.elements.sendBtn.style.display = 'flex';

                // 有内容就是正常结束，不显示任何提示
                if (!fullResponse) {
                    this.addErrorMessage('连接失败，请检查后端服务');
                }
            };
            
            // 监听完成事件
            this.eventSource.addEventListener('complete', () => {
                this.eventSource.close();
                this.isStreaming = false;
                this.elements.stopBtn.style.display = 'none';
                this.elements.sendBtn.style.display = 'flex';
                this.addSystemMessage('响应完成');
            });
            
        } catch (error) {
            console.error('流式请求失败:', error);
            this.isStreaming = false;
            this.elements.stopBtn.style.display = 'none';
            this.elements.sendBtn.style.display = 'flex';
            this.addErrorMessage('流式请求失败');
        }
    }
    
    stopStreaming() {
        if (this.eventSource) {
            this.eventSource.close();
            this.eventSource = null;
        }
        
        this.isStreaming = false;
        this.elements.stopBtn.style.display = 'none';
        this.elements.sendBtn.style.display = 'flex';
        
        this.addSystemMessage('已停止流式响应');
    }
    
    createAssistantMessageContainer() {
        const messageId = 'msg_' + Date.now();
        const messageElement = document.createElement('div');
        messageElement.className = 'message-bubble system';
        messageElement.id = messageId;
        messageElement.innerHTML = `
            <div class="message-header">
                <i class="fas ${this.getRoleIcon(this.currentRole)}"></i>
                <span>${this.getRoleName(this.currentRole)}</span>
            </div>
            <div class="message-content"></div>
            <div class="message-time">${this.getCurrentTime()}</div>
        `;
        
        this.elements.messagesContainer.appendChild(messageElement);
        this.scrollToBottom();
        
        return messageId;
    }
    
    appendToMessage(messageId, content) {
        const messageElement = document.getElementById(messageId);
        if (!messageElement) return;
        const contentElement = messageElement.querySelector('.message-content');
        contentElement.textContent += content;  // ← 用 textContent 直接追加纯文本
    }
    
    appendToMessageWithTyping(messageId, content) {
         const messageElement = document.getElementById(messageId);
         if (!messageElement) return;
         const contentElement = messageElement.querySelector('.message-content');
         // 移除打字机指示器
         const indicator = contentElement.querySelector('.typing-indicator');
         if (indicator) indicator.remove();
         // 直接追加文本节点，不转义不套娃
         contentElement.appendChild(document.createTextNode(content));
         // 重新加打字机指示器
         contentElement.insertAdjacentHTML('beforeend', '<div class="typing-indicator"><span></span><span></span><span></span></div>');
     }
    
    addUserMessage(content) {
        const messageElement = document.createElement('div');
        messageElement.className = 'message-bubble user';
        messageElement.innerHTML = `
            <div class="message-header">
                <i class="fas fa-user"></i>
                <span>你</span>
            </div>
            <div class="message-content">${this.escapeHtml(content)}</div>
            <div class="message-time">${this.getCurrentTime()}</div>
        `;
        
        this.elements.messagesContainer.appendChild(messageElement);
        this.scrollToBottom();
    }
    
    addAssistantMessage(content) {
        const messageElement = document.createElement('div');
        messageElement.className = 'message-bubble system';
        messageElement.innerHTML = `
            <div class="message-header">
                <i class="fas ${this.getRoleIcon(this.currentRole)}"></i>
                <span>${this.getRoleName(this.currentRole)}</span>
            </div>
            <div class="message-content">${this.renderContent(content)}</div>
            <div class="message-time">${this.getCurrentTime()}</div>
        `;
        
        this.elements.messagesContainer.appendChild(messageElement);
        this.scrollToBottom();
    }
    
    addSystemMessage(content) {
        const messageElement = document.createElement('div');
        messageElement.className = 'message-bubble system';
        messageElement.innerHTML = `
            <div class="message-header">
                <i class="fas fa-info-circle"></i>
                <span>系统提示</span>
            </div>
            <div class="message-content" style="background: #f0f9ff; color: #0369a1;">
                ${this.escapeHtml(content)}
            </div>
            <div class="message-time">${this.getCurrentTime()}</div>
        `;
        
        this.elements.messagesContainer.appendChild(messageElement);
        this.scrollToBottom();
    }
    
    addErrorMessage(content) {
        const messageElement = document.createElement('div');
        messageElement.className = 'message-bubble system';
        messageElement.innerHTML = `
            <div class="message-header">
                <i class="fas fa-exclamation-triangle"></i>
                <span>错误</span>
            </div>
            <div class="message-content" style="background: #fef2f2; color: #dc2626;">
                ${this.escapeHtml(content)}
            </div>
            <div class="message-time">${this.getCurrentTime()}</div>
        `;
        
        this.elements.messagesContainer.appendChild(messageElement);
        this.scrollToBottom();
    }
    
    renderContent(content) {
        // 简单Markdown渲染
        let rendered = this.escapeHtml(content);
        
        // 代码块
        rendered = rendered.replace(/```(\w+)?\n([\s\S]*?)```/g, (match, lang, code) => {
            return `<pre><code class="language-${lang || 'text'}">${this.escapeHtml(code.trim())}</code></pre>`;
        });
        
        // 内联代码
        rendered = rendered.replace(/`([^`]+)`/g, '<code>$1</code>');
        
        // 粗体
        rendered = rendered.replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>');
        
        // 斜体
        rendered = rendered.replace(/\*([^*]+)\*/g, '<em>$1</em>');
        
        // 链接
        rendered = rendered.replace(/\[([^\]]+)\]\(([^)]+)\)/g, '<a href="$2" target="_blank">$1</a>');
        
        // 换行
        rendered = rendered.replace(/\n/g, '<br>');
        
        return rendered;
    }
    
    renderMarkdown(element) {
        const content = element.innerHTML;
        element.innerHTML = this.renderContent(content);
    }
    
    clearChat() {
        if (!this.currentSessionId) return;
        
        if (confirm('确定要清空当前对话历史吗？')) {
            fetch(`${this.apiBaseUrl}/history/clear?sessionId=${this.currentSessionId}`, {
                method: 'POST'
            }).catch(console.error);
            
            // 清空UI（保留欢迎消息）
            const welcomeMessage = this.elements.messagesContainer.querySelector('.welcome-message');
            this.elements.messagesContainer.innerHTML = '';
            if (welcomeMessage) {
                this.elements.messagesContainer.appendChild(welcomeMessage);
            }
            
            this.addSystemMessage('对话历史已清空');
        }
    }
    
    exportChat() {
        const messages = [];
        const messageElements = this.elements.messagesContainer.querySelectorAll('.message-bubble');
        
        messageElements.forEach(element => {
            const header = element.querySelector('.message-header span').textContent;
            const content = element.querySelector('.message-content').textContent;
            const time = element.querySelector('.message-time').textContent;
            
            messages.push(`[${time}] ${header}: ${content}`);
        });
        
        const content = messages.join('\n\n');
        const blob = new Blob([content], { type: 'text/plain' });
        const url = URL.createObjectURL(blob);
        
        const a = document.createElement('a');
        a.href = url;
        a.download = `chat_export_${new Date().toISOString().slice(0, 10)}.txt`;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        URL.revokeObjectURL(url);
        
        this.addSystemMessage('对话已导出为文本文件');
    }
    
    async checkBackendStatus() {
        try {
            const response = await fetch(`${this.apiBaseUrl}/health`);
            const result = await response.json();
            
            if (result.status === 'UP') {
                this.elements.redisStatus.textContent = '已连接';
                this.elements.redisStatus.className = 'status-indicator connected';
                this.elements.modelStatus.textContent = result.model?.model || '未知';
            } else {
                this.elements.redisStatus.textContent = '未连接';
                this.elements.redisStatus.className = 'status-indicator disconnected';
            }
        } catch (error) {
            console.error('检查后端状态失败:', error);
            this.elements.redisStatus.textContent = '连接失败';
            this.elements.redisStatus.className = 'status-indicator disconnected';
        }
    }
    
    showLoading(show) {
        this.elements.loadingOverlay.style.display = show ? 'flex' : 'none';
    }
    
    scrollToBottom() {
        this.elements.messagesContainer.scrollTop = this.elements.messagesContainer.scrollHeight;
    }
    
    getCurrentTime() {
        const now = new Date();
        return now.toLocaleTimeString('zh-CN', { 
            hour: '2-digit', 
            minute: '2-digit',
            second: '2-digit'
        });
    }
    
    escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }
}

// 初始化应用
document.addEventListener('DOMContentLoaded', () => {
    window.chatApp = new ChatApp();
    
    // 添加示例消息
    setTimeout(() => {
        if (window.chatApp) {
            window.chatApp.addSystemMessage('💡 提示：您可以尝试以下问题：<br>' +
                '• "用Java写一个Hello World程序"<br>' +
                '• "翻译：Hello, how are you?"<br>' +
                '• "写一个关于AI的短故事"');
        }
    }, 1000);
});