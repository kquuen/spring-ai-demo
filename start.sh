#!/bin/bash

# Spring AI + 通义千问项目启动脚本
# 作者：峰
# 日期：2026-03-14

set -e

echo "=========================================="
echo "Spring AI + 通义千问项目启动脚本"
echo "=========================================="

# 检查环境变量
if [ -z "$DASHSCOPE_API_KEY" ]; then
    echo "⚠️  警告：未设置 DASHSCOPE_API_KEY 环境变量"
    echo "请在 .env 文件中设置，或运行："
    echo "  export DASHSCOPE_API_KEY=your-api-key"
    read -p "是否继续？(y/n): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 1
    fi
fi

# 检查 Redis
echo "🔍 检查 Redis 服务..."
if ! command -v redis-cli &> /dev/null; then
    echo "❌ Redis 未安装，请先安装 Redis"
    echo "推荐使用 Docker: docker run -d -p 6379:6379 redis:alpine"
    exit 1
fi

if ! redis-cli ping &> /dev/null; then
    echo "⚠️  Redis 服务未运行，正在尝试启动..."
    
    # 尝试启动 Redis
    if command -v systemctl &> /dev/null; then
        sudo systemctl start redis
    elif command -v service &> /dev/null; then
        sudo service redis start
    else
        echo "❌ 无法自动启动 Redis，请手动启动 Redis 服务"
        exit 1
    fi
    
    sleep 2
    
    if ! redis-cli ping &> /dev/null; then
        echo "❌ Redis 启动失败，请检查 Redis 配置"
        exit 1
    fi
fi

echo "✅ Redis 服务正常"

# 检查 Java
echo "🔍 检查 Java 环境..."
if ! command -v java &> /dev/null; then
    echo "❌ Java 未安装，请安装 Java 17+"
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | head -1 | cut -d'"' -f2)
echo "✅ Java 版本: $JAVA_VERSION"

# 检查 Maven
echo "🔍 检查 Maven..."
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven 未安装，请安装 Maven 3.6+"
    exit 1
fi

echo "✅ Maven 可用"

# 构建项目
echo "🚀 构建项目..."
mvn clean package -DskipTests

if [ $? -ne 0 ]; then
    echo "❌ 项目构建失败"
    exit 1
fi

echo "✅ 项目构建成功"

# 启动后端
echo "🚀 启动后端服务..."
JAR_FILE=$(find target -name "*.jar" -type f | head -1)

if [ -z "$JAR_FILE" ]; then
    echo "❌ 未找到可执行的 JAR 文件"
    exit 1
fi

echo "📦 使用 JAR 文件: $JAR_FILE"

# 设置环境变量
export SPRING_PROFILES_ACTIVE=dev
export REDIS_HOST=localhost
export REDIS_PORT=6379

# 启动应用
java -jar "$JAR_FILE" &

BACKEND_PID=$!
echo "✅ 后端服务已启动，PID: $BACKEND_PID"

# 等待后端启动
echo "⏳ 等待后端服务启动..."
sleep 10

# 检查后端健康
if curl -s http://localhost:8080/api/chat/health | grep -q "UP"; then
    echo "✅ 后端服务健康检查通过"
else
    echo "❌ 后端服务健康检查失败"
    kill $BACKEND_PID 2>/dev/null
    exit 1
fi

# 启动前端
echo "🚀 启动前端服务..."
cd frontend

# 检查是否有 Python
if command -v python3 &> /dev/null; then
    echo "🌐 使用 Python 启动前端服务器..."
    python3 -m http.server 3000 &
elif command -v python &> /dev/null; then
    echo "🌐 使用 Python 启动前端服务器..."
    python -m http.server 3000 &
elif command -v node &> /dev/null; then
    echo "🌐 使用 Node.js 启动前端服务器..."
    npx http-server -p 3000 &
else
    echo "⚠️  未找到合适的 HTTP 服务器，请手动启动前端"
    echo "前端文件位于: frontend/"
    echo "可以使用任何 HTTP 服务器，例如:"
    echo "  cd frontend && python -m http.server 3000"
fi

FRONTEND_PID=$!
echo "✅ 前端服务已启动，PID: $FRONTEND_PID"

# 显示访问信息
echo ""
echo "=========================================="
echo "🎉 项目启动成功！"
echo "=========================================="
echo ""
echo "🌐 访问地址:"
echo "   前端界面: http://localhost:3000"
echo "   后端API:  http://localhost:8080"
echo ""
echo "📋 API 文档:"
echo "   健康检查: curl http://localhost:8080/api/chat/health"
echo "   创建会话: curl http://localhost:8080/api/chat/session"
echo "   角色对话: curl 'http://localhost:8080/api/chat/role?sessionId=xxx&message=你好&roleId=assistant'"
echo ""
echo "🛑 停止服务:"
echo "   kill $BACKEND_PID $FRONTEND_PID"
echo "   或按 Ctrl+C 停止当前脚本"
echo ""
echo "=========================================="

# 等待用户中断
wait $BACKEND_PID $FRONTEND_PID