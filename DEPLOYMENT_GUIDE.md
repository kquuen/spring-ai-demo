# Spring AI + 通义千问项目部署指南

## 🚀 快速开始

### 方式1：一键启动（推荐）
```bash
# Windows
start.bat

# Linux/Mac
chmod +x start.sh
./start.sh
```

### 方式2：Docker Compose部署
```bash
# 1. 创建 .env 文件
echo "DASHSCOPE_API_KEY=你的API密钥" > .env

# 2. 启动所有服务
docker-compose up -d

# 3. 查看服务状态
docker-compose ps

# 4. 访问应用
# 前端: http://localhost:3000
# 后端API: http://localhost:8080
```

### 方式3：手动部署
```bash
# 1. 安装依赖
# - Java 17+
# - Maven 3.6+
# - Redis 6+

# 2. 设置环境变量
export DASHSCOPE_API_KEY=你的API密钥
export REDIS_HOST=localhost
export REDIS_PORT=6379

# 3. 启动Redis
redis-server

# 4. 构建项目
mvn clean package -DskipTests

# 5. 启动后端
java -jar target/demo-0.0.1-SNAPSHOT.jar

# 6. 启动前端
cd frontend
python -m http.server 3000
```

## 🔧 环境要求

### 必需组件
1. **Java 17+** - Spring Boot 3.2.5需要
2. **Maven 3.6+** - 项目构建工具
3. **Redis 6+** - 会话存储
4. **通义千问API密钥** - 从阿里云DashScope获取

### 可选组件
1. **Docker & Docker Compose** - 容器化部署
2. **Python 3+ 或 Node.js** - 前端HTTP服务器
3. **Git** - 版本控制

## 📝 配置说明

### 1. 获取通义千问API密钥
1. 访问 [阿里云DashScope](https://dashscope.aliyun.com/)
2. 注册/登录账号
3. 创建API密钥
4. 设置环境变量：
   ```bash
   # Linux/Mac
   export DASHSCOPE_API_KEY=sk-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
   
   # Windows
   set DASHSCOPE_API_KEY=sk-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
   ```

### 2. Redis配置
默认配置：
- 主机：localhost
- 端口：6379
- 密码：无
- 数据库：0

如需修改，编辑 `application.yaml`：
```yaml
spring:
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}
      database: 0
```

### 3. 应用配置
主要配置文件：
- `application.yaml` - 开发环境配置
- `application-docker.yaml` - Docker环境配置
- `application-test.yaml` - 测试环境配置

## 🌐 访问地址

### 本地开发
- **前端界面**: http://localhost:3000
- **后端API**: http://localhost:8080
- **API文档**: http://localhost:8080/swagger-ui.html (需要添加Swagger依赖)

### Docker部署
- **前端界面**: http://localhost:3000
- **后端API**: http://localhost:8080
- **Redis管理**: http://localhost:8081 (RedisInsight)

## 📊 健康检查

### API端点
```bash
# 应用健康
curl http://localhost:8080/api/chat/health

# Redis健康
curl http://localhost:8080/api/chat/health/redis

# 模型连接
curl http://localhost:8080/api/chat/health/model
```

### 预期响应
```json
{
  "status": "UP",
  "components": {
    "redis": {
      "status": "UP"
    },
    "model": {
      "status": "UP"
    }
  }
}
```

## 🔍 故障排除

### 常见问题

#### 1. Redis连接失败
```
Error: Unable to connect to Redis
```
**解决方案**：
```bash
# 检查Redis是否运行
redis-cli ping

# 启动Redis
redis-server

# 或使用Docker
docker run -d -p 6379:6379 redis:alpine
```

#### 2. API密钥无效
```
Error: Invalid API key
```
**解决方案**：
1. 确认API密钥是否正确
2. 检查环境变量是否设置
3. 确认DashScope账户余额充足

#### 3. 端口冲突
```
Error: Port 8080 already in use
```
**解决方案**：
```bash
# 查找占用进程
netstat -ano | findstr :8080

# 或修改端口
# 在 application.yaml 中修改
server:
  port: 8081
```

#### 4. 前端无法连接后端
```
Error: Failed to fetch
```
**解决方案**：
1. 检查后端是否运行
2. 确认CORS配置正确
3. 检查网络连接

## 📈 监控与日志

### 日志文件
- 控制台输出
- `logs/` 目录下的日志文件

### 监控端点
- `/actuator/health` - 健康检查
- `/actuator/info` - 应用信息
- `/actuator/metrics` - 性能指标

## 🔒 安全建议

### 生产环境部署
1. **使用HTTPS** - 配置SSL证书
2. **API密钥管理** - 使用密钥管理服务
3. **防火墙配置** - 限制访问端口
4. **定期备份** - Redis数据备份
5. **监控告警** - 设置性能监控

### 环境变量安全
```bash
# 不要硬编码密钥
# ❌ 错误做法
export DASHSCOPE_API_KEY=sk-xxx

# ✅ 正确做法 - 使用.env文件
echo "DASHSCOPE_API_KEY=sk-xxx" > .env
docker-compose --env-file .env up -d
```

## 🚢 生产部署

### 使用Docker Compose
```bash
# 1. 准备环境文件
cat > .env << EOF
DASHSCOPE_API_KEY=sk-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
REDIS_PASSWORD=your_redis_password
SPRING_PROFILES_ACTIVE=prod
EOF

# 2. 启动服务
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d

# 3. 查看日志
docker-compose logs -f
```

### 使用Kubernetes
```yaml
# deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: spring-ai-demo
spec:
  replicas: 3
  selector:
    matchLabels:
      app: spring-ai-demo
  template:
    metadata:
      labels:
        app: spring-ai-demo
    spec:
      containers:
      - name: app
        image: spring-ai-demo:latest
        env:
        - name: DASHSCOPE_API_KEY
          valueFrom:
            secretKeyRef:
              name: api-secrets
              key: dashscope-api-key
        ports:
        - containerPort: 8080
```

## 📞 支持与帮助

### 文档资源
1. [Spring AI官方文档](https://docs.spring.io/spring-ai/reference/)
2. [阿里云DashScope文档](https://help.aliyun.com/zh/dashscope/)
3. [Redis官方文档](https://redis.io/documentation)

### 问题反馈
1. 检查项目 [Issues](https://github.com/kquuen/spring-ai-demo/issues)
2. 提交新的Issue
3. 查看 [FAQ](./FAQ.md)

---

**🎉 部署成功！现在你可以访问 http://localhost:3000 开始使用智能聊天系统了！**