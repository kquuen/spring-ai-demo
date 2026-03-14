# Spring AI + 通义千问智能聊天助手

基于 Spring AI 框架与阿里云通义千问大模型构建的智能聊天系统，支持多角色对话、流式响应和会话管理。

## ✨ 功能特性

### 🎯 核心功能
- **多角色对话**：预置智能助手、代码专家、翻译专家、创意写手等多种角色
- **流式响应**：基于 SSE 实现打字机效果的流式输出
- **会话管理**：Redis 持久化存储对话历史，支持多轮上下文
- **自定义角色**：支持创建和管理自定义系统提示词角色

### 🚀 技术栈
- **后端**：Spring Boot 3.2 + Spring AI + Redis
- **前端**：HTML5 + CSS3 + JavaScript (原生)
- **AI模型**：阿里云通义千问 (Qwen-Turbo/Qwen-Max)
- **通信协议**：REST API + Server-Sent Events (SSE)

## 📋 系统要求

### 环境要求
- Java 17+
- Maven 3.6+
- Redis 6.0+
- Node.js 14+ (仅前端开发需要)

### API密钥
需要阿里云 DashScope API Key：
- 访问 [阿里云 DashScope](https://dashscope.aliyun.com/) 获取 API Key
- 支持模型：qwen-turbo、qwen-max、qwen-plus 等

## 🚀 快速开始

### 1. 克隆项目
```bash
git clone https://github.com/kquuen/spring-ai-demo.git
cd spring-ai-demo
```

### 2. 配置环境变量
创建 `.env` 文件（或直接修改 application.yaml）：
```bash
# 阿里云 DashScope API Key
DASHSCOPE_API_KEY=sk-your-api-key-here

# Redis 配置（可选，默认使用 localhost:6379）
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=
```

### 3. 启动 Redis
```bash
# 使用 Docker 启动 Redis
docker run -d --name redis-spring-ai -p 6379:6379 redis:alpine

# 或使用本地安装的 Redis
redis-server
```

### 4. 启动后端服务
```bash
# 使用 Maven 启动
mvn spring-boot:run

# 或打包后运行
mvn clean package
java -jar target/demo-0.0.1-SNAPSHOT.jar
```

### 5. 启动前端
```bash
# 进入前端目录
cd frontend

# 使用任何 HTTP 服务器，例如：
# Python 3
python -m http.server 3000

# 或使用 Node.js 的 http-server
npx http-server -p 3000
```

### 6. 访问应用
- 后端 API：http://localhost:8080
- 前端界面：http://localhost:3000

## 📖 API 文档

### 基础端点
- `GET /api/chat/health` - 健康检查
- `GET /api/chat/session` - 生成会话ID

### 聊天接口
- `GET/POST /api/chat/role` - 角色对话（普通响应）
- `GET /api/chat/role/stream` - 角色对话（流式响应）
- `GET/POST /api/chat/history` - 历史对话（普通响应）
- `GET /api/chat/history/stream` - 历史对话（流式响应）
- `POST /api/chat/history/clear` - 清空对话历史

### 角色管理
- `GET /api/chat/roles` - 获取所有角色
- `GET /api/chat/roles/{roleId}` - 获取特定角色
- `POST /api/chat/roles` - 创建自定义角色
- `DELETE /api/chat/roles/{roleId}` - 删除自定义角色

## 🎨 前端功能

### 界面特性
- **响应式设计**：适配桌面和移动设备
- **实时聊天**：支持流式打字机效果
- **会话管理**：创建、切换、清空会话
- **角色切换**：一键切换不同AI角色
- **消息导出**：导出对话历史为文本文件

### 使用说明
1. 打开前端页面
2. 系统会自动创建新会话
3. 从左侧选择AI角色
4. 在输入框输入问题，按 Enter 发送
5. 可随时切换角色或创建新会话

## 🔧 配置说明

### 后端配置 (application.yaml)
```yaml
spring:
  ai:
    openai:
      base-url: https://dashscope.aliyuncs.com/compatible-mode/v1
      api-key: ${DASHSCOPE_API_KEY}
      chat:
        options:
          model: qwen-turbo  # 可改为 qwen-max、qwen-plus 等
          temperature: 0.7   # 创意性：0-1，越高越有创意
          max-tokens: 2000   # 最大响应长度
  
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
```

### 预置角色
系统预置了4个角色：
1. **智能助手** (assistant) - 通用问答
2. **代码专家** (coder) - 编程问题，temperature=0.3
3. **翻译专家** (translator) - 中英翻译，temperature=0.5
4. **创意写手** (writer) - 内容创作，temperature=0.9

## 🧪 测试

### 运行单元测试
```bash
mvn test
```

### API 测试
使用 curl 测试基础功能：
```bash
# 健康检查
curl http://localhost:8080/api/chat/health

# 创建会话
curl http://localhost:8080/api/chat/session

# 角色对话
curl "http://localhost:8080/api/chat/role?sessionId=xxx&message=你好&roleId=assistant"

# 流式对话
curl "http://localhost:8080/api/chat/role/stream?sessionId=xxx&message=你好&roleId=coder"
```

## 📁 项目结构

```
spring-ai-demo/
├── src/main/java/com/example/demo/
│   ├── config/              # 配置类
│   │   ├── RedisConfig.java
│   │   └── AiConfig.java
│   ├── controller/          # 控制器
│   │   └── ChatController.java
│   ├── service/             # 业务服务
│   │   ├── ChatService.java
│   │   ├── RedisChatContext.java
│   │   ├── RoleService.java
│   │   └── ChatContext.java (旧版)
│   ├── model/               # 数据模型
│   │   ├── ChatRequest.java
│   │   ├── ChatResponse.java
│   │   └── RoleConfig.java
│   └── DemoApplication.java # 主应用
├── src/main/resources/
│   ├── application.yaml     # 主配置文件
│   └── application-dev.yaml # 开发配置
├── frontend/                # 前端代码
│   ├── index.html          # 主页面
│   ├── style.css           # 样式表
│   └── script.js           # 业务逻辑
├── pom.xml                 # Maven配置
└── README.md              # 项目文档
```

## 🔄 部署

### Docker 部署
```bash
# 构建 Docker 镜像
docker build -t spring-ai-qwen .

# 运行容器
docker run -d \
  -p 8080:8080 \
  -e DASHSCOPE_API_KEY=your-key \
  -e REDIS_HOST=redis \
  --name spring-ai-app \
  spring-ai-qwen
```

### 云部署建议
1. **后端**：部署到阿里云 ECS 或 Kubernetes
2. **Redis**：使用阿里云 Redis 或自建
3. **前端**：部署到对象存储（OSS）或 CDN
4. **域名**：配置 SSL 证书启用 HTTPS

## 🐛 故障排除

### 常见问题

1. **API Key 错误**
   ```
   Error: Invalid API Key
   ```
   解决方案：检查 DashScope API Key 是否正确，确保有足够余额

2. **Redis 连接失败**
   ```
   Connection refused: localhost:6379
   ```
   解决方案：启动 Redis 服务，或修改配置使用远程 Redis

3. **跨域问题**
   ```
   CORS policy: No 'Access-Control-Allow-Origin'
   ```
   解决方案：前端和后端需要在同一域名下，或正确配置 CORS

4. **流式响应中断**
   ```
   EventSource closed
   ```
   解决方案：检查网络稳定性，增加后端超时时间

### 日志查看
```bash
# 查看应用日志
tail -f logs/spring-ai-qwen.log

# 查看 Redis 连接
redis-cli monitor
```

## 📈 性能优化

### 建议配置
1. **Redis 连接池**：根据并发量调整连接数
2. **API 限流**：实现请求频率限制
3. **缓存策略**：缓存常用角色配置
4. **会话清理**：定期清理过期会话

### 监控指标
- 请求响应时间
- Redis 内存使用
- API 调用次数
- 并发会话数

## 🤝 贡献指南

欢迎提交 Issue 和 Pull Request！

1. Fork 项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 🙏 致谢

- [Spring AI](https://spring.io/projects/spring-ai) - Spring 官方 AI 框架
- [阿里云通义千问](https://dashscope.aliyun.com/) - 大语言模型服务
- [Redis](https://redis.io/) - 内存数据存储
- 所有贡献者和使用者

## 📞 联系方式

如有问题或建议，请通过以下方式联系：

- GitHub Issues: [项目 Issues](https://github.com/kquuen/spring-ai-demo/issues)
- Email: 2356250854@qq.com

---

**🚀 开始你的 AI 对话之旅吧！**