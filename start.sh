#!/bin/bash
set -e

echo "========================================="
echo "  国风麻将 - 本地启动脚本"
echo "========================================="

MYSQL_PORT=${MYSQL_PORT:-45213}
BACKEND_PORT=${BACKEND_PORT:-38942}

check_command() {
  if ! command -v "$1" &> /dev/null; then
    echo "❌ 未找到命令: $1，请先安装。"
    exit 1
  fi
}

echo ""
echo "[1/3] 检查依赖..."
check_command docker
check_command mvn
check_command java

echo ""
echo "[2/3] 启动 MySQL 容器 (端口 $MYSQL_PORT)..."
docker rm -f mahjong-mysql 2>/dev/null || true
docker run -d \
  --name mahjong-mysql \
  -e MYSQL_ROOT_PASSWORD=root123 \
  -e MYSQL_DATABASE=mahjong_db \
  -e MYSQL_CHARACTER_SET_SERVER=utf8mb4 \
  -e MYSQL_COLLATION_SERVER=utf8mb4_unicode_ci \
  -p "${MYSQL_PORT}:3306" \
  -v "$(pwd)/database-mysql/init.sql:/docker-entrypoint-initdb.d/init.sql" \
  mysql:8.0

echo "⏳ 等待 MySQL 就绪..."
for i in $(seq 1 30); do
  if docker exec mahjong-mysql mysqladmin ping -uroot -proot123 --silent 2>/dev/null; then
    echo "✅ MySQL 已就绪"
    break
  fi
  if [ "$i" -eq 30 ]; then
    echo "❌ MySQL 启动超时"
    exit 1
  fi
  sleep 2
done

echo ""
echo "[3/3] 构建并启动后端 (端口 $BACKEND_PORT)..."
cd backend
mvn clean package -DskipTests -q
java -jar target/*.jar \
  --server.port="${BACKEND_PORT}" \
  --spring.datasource.url="jdbc:mysql://localhost:${MYSQL_PORT}/mahjong_db?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai" &

BACKEND_PID=$!
cd ..

echo ""
echo "========================================="
echo "  ✅ 服务已启动"
echo "  后端: http://localhost:${BACKEND_PORT}"
echo "  MySQL: localhost:${MYSQL_PORT}"
echo "  测试账号: admin/admin123, user/user123"
echo ""
echo "  前端: 使用微信开发者工具导入 frontend-mp 目录"
echo "  按 Ctrl+C 停止服务"
echo "========================================="

cleanup() {
  echo ""
  echo "正在停止服务..."
  kill $BACKEND_PID 2>/dev/null || true
  docker rm -f mahjong-mysql 2>/dev/null || true
  echo "✅ 已停止所有服务"
}

trap cleanup EXIT INT TERM
wait $BACKEND_PID
