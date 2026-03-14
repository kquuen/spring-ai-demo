@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

echo ==========================================
echo Spring AI + 通义千问项目启动脚本
echo ==========================================

REM 检查环境变量
if "%DASHSCOPE_API_KEY%"=="" (
    echo ⚠️  警告：未设置 DASHSCOPE_API_KEY 环境变量
    echo 请在系统环境变量中设置，或运行：
    echo   set DASHSCOPE_API_KEY=your-api-key
    set /p continue="是否继续？(y/n): "
    if /i not "!continue!"=="y" (
        exit /b 1
    )
)

REM 检查 Redis
echo 🔍 检查 Redis 服务...
where redis-cli >nul 2>nul
if errorlevel 1 (
    echo ❌ Redis 未安装，请先安装 Redis
    echo 推荐使用 Docker: docker run -d -p 6379:6379 redis:alpine
    pause
    exit /b 1
)

redis-cli ping >nul 2>nul
if errorlevel 1 (
    echo ⚠️  Redis 服务未运行，请手动启动 Redis
    echo 1. 打开新的命令提示符
    echo 2. 运行: redis-server
    echo 3. 然后重新运行此脚本
    pause
    exit /b 1
)

echo ✅ Redis 服务正常

REM 检查 Java
echo 🔍 检查 Java 环境...
where java >nul 2>nul
if errorlevel 1 (
    echo ❌ Java 未安装，请安装 Java 17+
    pause
    exit /b 1
)

for /f "tokens=3" %%i in ('java -version 2^>^&1 ^| findstr /i "version"') do (
    set JAVA_VERSION=%%i
)
echo ✅ Java 版本: !JAVA_VERSION!

REM 检查 Maven
echo 🔍 检查 Maven...
where mvn >nul 2>nul
if errorlevel 1 (
    echo ❌ Maven 未安装，请安装 Maven 3.6+
    pause
    exit /b 1
)

echo ✅ Maven 可用

REM 构建项目
echo 🚀 构建项目...
call mvn clean package -DskipTests

if errorlevel 1 (
    echo ❌ 项目构建失败
    pause
    exit /b 1
)

echo ✅ 项目构建成功

REM 启动后端
echo 🚀 启动后端服务...
for /r target %%i in (*.jar) do (
    set JAR_FILE=%%i
    goto found_jar
)

:found_jar
if "!JAR_FILE!"=="" (
    echo ❌ 未找到可执行的 JAR 文件
    pause
    exit /b 1
)

echo 📦 使用 JAR 文件: !JAR_FILE!

REM 设置环境变量
set SPRING_PROFILES_ACTIVE=dev
set REDIS_HOST=localhost
set REDIS_PORT=6379

REM 启动应用
start "Spring AI Backend" cmd /c "java -jar "!JAR_FILE!""

echo ✅ 后端服务已启动

REM 等待后端启动
echo ⏳ 等待后端服务启动...
timeout /t 10 /nobreak >nul

REM 检查后端健康
curl -s http://localhost:8080/api/chat/health | findstr "UP" >nul
if errorlevel 1 (
    echo ❌ 后端服务健康检查失败
    pause
    exit /b 1
)

echo ✅ 后端服务健康检查通过

REM 启动前端
echo 🚀 启动前端服务...
cd frontend

REM 检查是否有 Python
where python >nul 2>nul
if not errorlevel 1 (
    echo 🌐 使用 Python 启动前端服务器...
    start "Spring AI Frontend" cmd /c "python -m http.server 3000"
) else (
    where node >nul 2>nul
    if not errorlevel 1 (
        echo 🌐 使用 Node.js 启动前端服务器...
        start "Spring AI Frontend" cmd /c "npx http-server -p 3000"
    ) else (
        echo ⚠️  未找到合适的 HTTP 服务器，请手动启动前端
        echo 前端文件位于: frontend/
        echo 可以使用任何 HTTP 服务器
    )
)

echo ✅ 前端服务已启动

REM 显示访问信息
echo.
echo ==========================================
echo 🎉 项目启动成功！
echo ==========================================
echo.
echo 🌐 访问地址：
echo    前端界面: http://localhost:3000
echo    后端API:  http://localhost:8080
echo.
echo 📋 API 文档：
echo    健康检查: curl http://localhost:8080/api/chat/health
echo    创建会话: curl http://localhost:8080/api/chat/session
echo    角色对话: curl "http://localhost:8080/api/chat/role?sessionId=xxx^&message=你好^&roleId=assistant"
echo.
echo 🛑 停止服务：
echo    关闭所有打开的命令提示符窗口
echo.
echo ==========================================
echo.
pause