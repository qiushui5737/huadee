@echo off
title 启动政府平台

echo ================================
echo   启动政府平台项目
echo ================================
echo.

echo [1/2] 启动后端服务...
cd /d "C:\Users\Administrator\Desktop\作业\huadishixi\huadee\gov-platform"
start "后端服务" cmd /k "mvn spring-boot:run"

echo [等待后端启动...]
timeout /t 10 /nobreak >nul

echo.
echo [2/2] 启动前端服务...
cd /d "C:\Users\Administrator\Desktop\作业\huadishixi\huadee\gov-frontend"
start "前端服务" cmd /k "npm run dev"

echo.
echo 启动完成！
echo 后端服务: http://localhost:8080
echo 前端服务: http://localhost:3000
echo.
pause