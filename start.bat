@echo off
chcp 65001 >nul 2>&1
setlocal enabledelayedexpansion

echo =========================================
echo   国风麻将 - 本地启动脚本 (Windows)
echo =========================================

set MYSQL_PORT=45213
set BACKEND_PORT=38942

echo.
echo [1/3] 检查依赖...
where docker >nul 2>&1 || (echo 未找到 docker，请先安装。 & exit /b 1)
where mvn >nul 2>&1 || (echo 未找到 mvn，请先安装。 & exit /b 1)
where java >nul 2>&1 || (echo 未找到 java，请先安装。 & exit /b 1)

echo.
echo [2/3] 启动 MySQL 容器 (端口 %MYSQL_PORT%)...
docker rm -f mahjong-mysql >nul 2>&1
docker run -d --name mahjong-mysql ^
  -e MYSQL_ROOT_PASSWORD=root123 ^
  -e MYSQL_DATABASE=mahjong_db ^
  -e MYSQL_CHARACTER_SET_SERVER=utf8mb4 ^
  -e MYSQL_COLLATION_SERVER=utf8mb4_unicode_ci ^
  -p %MYSQL_PORT%:3306 ^
  -v "%cd%\database-mysql\init.sql:/docker-entrypoint-initdb.d/init.sql" ^
  mysql:8.0

echo 等待 MySQL 就绪...
set /a count=0
:wait_mysql
set /a count+=1
if %count% gtr 30 (echo MySQL 启动超时 & exit /b 1)
docker exec mahjong-mysql mysqladmin ping -uroot -proot123 --silent >nul 2>&1
if errorlevel 1 (timeout /t 2 /nobreak >nul & goto :wait_mysql)
echo MySQL 已就绪

echo.
echo [3/3] 构建并启动后端 (端口 %BACKEND_PORT%)...
cd backend
call mvn clean package -DskipTests -q
start /b java -jar target\mahjong-backend-1.0.0.jar ^
  --server.port=%BACKEND_PORT% ^
  --spring.datasource.url="jdbc:mysql://localhost:%MYSQL_PORT%/mahjong_db?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai"
cd ..

echo.
echo =========================================
echo   服务已启动
echo   后端: http://localhost:%BACKEND_PORT%
echo   MySQL: localhost:%MYSQL_PORT%
echo   测试账号: admin/admin123, user/user123
echo.
echo   前端: 使用微信开发者工具导入 frontend-mp 目录
echo   按 Ctrl+C 停止服务
echo =========================================
pause
