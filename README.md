# 国风麻将微信小程序

基于通用麻将规则的单机对战微信小程序，配合 Spring Boot 后端进行牌局逻辑计算，MySQL 存储游戏记录与用户设置。

## 1. How to Run

### 使用 Docker（推荐）

```bash
docker-compose up --build -d
```

后端服务启动后访问 `http://localhost:38942`

### 本地开发

**后端**
```bash
cd backend
./mvnw spring-boot:run
```

**数据库**

确保 MySQL 运行在端口 45213，并执行初始化脚本：
```bash
mysql -u root -p -P 45213 < database-mysql/init.sql
```

**微信小程序**

1. 打开微信开发者工具
2. 导入 `frontend-mp` 目录
3. 使用测试 AppID
4. 在 `utils/api.js` 中确认 `baseUrl` 指向后端地址

## 2. Services

| 服务 | 端口 | 说明 |
|------|------|------|
| Spring Boot 后端 | 38942 | 游戏逻辑 API |
| MySQL 数据库 | 45213 | 数据持久化 |
| 微信小程序 | — | 微信开发者工具运行 |

## 3. 测试账号

| 角色 | 用户名 | 密码 | 说明 |
|------|--------|------|------|
| 管理员 | admin | admin123 | 拥有所有权限 |
| 普通用户 | user | user123 | 基本功能权限 |

## 4. 题目内容

写一个麻将的微信小程序。

## 5. 项目结构

```
label-01834/
├── backend/                 # Spring Boot 后端
│   ├── src/main/java/com/mahjong/
│   │   ├── config/          # 配置（CORS、安全、JWT）
│   │   ├── common/          # 通用组件（Result、异常处理）
│   │   ├── controller/      # 控制器（User、Game、Setting）
│   │   ├── service/         # 业务逻辑
│   │   │   └── engine/      # 麻将引擎（核心算法）
│   │   ├── entity/          # 数据实体
│   │   ├── dto/             # 数据传输对象
│   │   └── mapper/          # 数据访问层
│   ├── src/main/resources/
│   │   └── application.yml
│   ├── Dockerfile
│   └── pom.xml
├── frontend-mp/             # 微信小程序
│   ├── pages/
│   │   ├── login/           # 登录/注册
│   │   ├── index/           # 首页
│   │   ├── game/            # 游戏牌桌
│   │   ├── history/         # 战绩记录
│   │   └── settings/        # 游戏设置
│   ├── components/tile/     # 麻将牌组件
│   ├── utils/               # 工具函数、API 封装
│   ├── images/              # 图标资源
│   ├── app.js / app.json / app.wxss
│   └── project.config.json
├── database-mysql/          # 数据库
│   └── init.sql             # 建表 + 示例数据
├── docs/
│   ├── project_design.md    # 设计文档
│   └── api_documentation.md # API 文档
├── docker-compose.yml       # 容器编排
└── README.md
```

## 6. 功能清单

- 用户登录/注册（JWT 认证）
- 单机麻将对战（1 人 vs 3 AI）
- 通用麻将规则（136 张牌，吃/碰/杠/胡）
- 自动理牌
- AI 智能出牌策略
- 游戏历史记录
- 战绩统计（胜率、场均得分）
- 游戏设置（AI 速度、音效、自动理牌）
- 国风视觉设计（楷书字体、翠绿牌桌、古韵配色）

## 7. 开发与数据说明

本项目使用种子数据用于演示与开发联调：
- **数据来源**: `database-mysql/init.sql`
- **用途**: 开发调试、演示展示
- **内容**: 2 个测试账号、14 条游戏记录、8 条默认设置
- **生产环境**: 接入真实数据后替换即可

## 编码说明

本项目所有文件使用 UTF-8 编码，确保中文正常显示：
- 源代码：UTF-8 without BOM
- 数据库：utf8mb4
- 配置文件：UTF-8
