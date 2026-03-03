# API 接口文档 - 国风麻将后端

Base URL: `http://localhost:38942`

所有接口（除登录/注册外）需在请求头携带 `Authorization: Bearer {token}`

## 统一响应格式

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {}
}
```

---

## 1. 用户模块

### POST /api/user/login
用户登录

**请求体:**
```json
{ "nickname": "admin", "password": "admin123" }
```

**响应:**
```json
{
  "code": 200,
  "message": "登录成功",
  "data": { "token": "eyJ...", "userId": 1, "nickname": "admin" }
}
```

### POST /api/user/register
用户注册

**请求体:**
```json
{ "nickname": "newplayer", "password": "pass1234" }
```

**响应:** 同登录

### GET /api/user/info
获取当前用户信息（需认证）

**响应:**
```json
{
  "code": 200,
  "data": {
    "id": 1, "nickname": "admin", "avatar": "",
    "totalGames": 15, "winGames": 8,
    "createdAt": "2026-02-25 10:00:00"
  }
}
```

---

## 2. 游戏模块

### POST /api/game/start
创建新游戏

**响应:**
```json
{
  "code": 200,
  "message": "游戏开始",
  "data": {
    "gameId": "a1b2c3d4",
    "playerHand": ["wan1", "wan3", "tiao5", ...],
    "playerMelds": [],
    "playerDiscards": [],
    "drawnTile": "tong2",
    "currentPlayer": 0,
    "phase": "discard",
    "availableActions": [],
    "lastDiscard": null,
    "lastDiscardPlayer": -1,
    "wallRemaining": 83,
    "winner": -1,
    "aiPlayers": [
      { "seat": 1, "name": "AI-东", "tileCount": 13, "melds": [], "discards": [] },
      { "seat": 2, "name": "AI-南", "tileCount": 13, "melds": [], "discards": [] },
      { "seat": 3, "name": "AI-西", "tileCount": 13, "melds": [], "discards": [] }
    ]
  }
}
```

### POST /api/game/{gameId}/discard?tile={tileCode}
玩家出牌

**参数:** `tile` — 牌编码（如 `wan3`, `tiao5`）

**响应:** 返回更新后的 GameState（同 start）

### POST /api/game/{gameId}/action
玩家操作

**请求体:**
```json
{ "action": "pong" }
{ "action": "kong", "tile": "wan5" }
{ "action": "chi", "tiles": ["wan4", "wan6"] }
{ "action": "win" }
{ "action": "pass" }
```

**action 取值:**
| 操作 | 说明 | 附加参数 |
|------|------|---------|
| pong | 碰牌 | 无 |
| kong | 杠牌 | tile (暗杠时指定) |
| chi | 吃牌 | tiles (两张手牌) |
| win | 胡牌 | 无 |
| pass | 跳过 | 无 |

**响应:** 返回更新后的 GameState

### GET /api/game/{gameId}/state
获取当前游戏状态

**响应:** 返回 GameState

### GET /api/game/history
获取历史记录（最近50局）

**响应:**
```json
{
  "code": 200,
  "data": [
    { "id": 1, "userId": 1, "result": "win", "rounds": 32, "score": 10, "duration": 480, "createdAt": "2026-02-25 14:30:00" }
  ]
}
```

### GET /api/game/stats
获取统计数据

**响应:**
```json
{
  "code": 200,
  "data": { "total": 15, "wins": 8, "losses": 5, "draws": 2, "avgScore": 3.3 }
}
```

---

## 3. 设置模块

### GET /api/settings
获取用户设置

**响应:**
```json
{
  "code": 200,
  "data": { "ai_speed": "normal", "sound_enabled": "true", "auto_sort": "true", "theme": "classic" }
}
```

### PUT /api/settings
更新用户设置

**请求体:**
```json
{ "ai_speed": "fast", "sound_enabled": "false" }
```

---

## 牌面编码规则

| 花色 | 编码前缀 | 取值范围 | 示例 |
|------|---------|---------|------|
| 万子 | wan | 1-9 | wan1, wan9 |
| 条子 | tiao | 1-9 | tiao3, tiao7 |
| 筒子 | tong | 1-9 | tong5, tong2 |
| 风牌 | feng | 1-4 (东南西北) | feng1(东), feng4(北) |
| 箭牌 | jian | 1-3 (中发白) | jian1(中), jian3(白) |

共 34 种牌面，每种 4 张，总计 136 张。

## 游戏阶段 (phase)

| 阶段 | 说明 |
|------|------|
| dealing | 发牌中 |
| draw | 等待摸牌 |
| discard | 等待出牌 |
| action | 等待玩家操作（吃/碰/杠/胡/过） |
| ai_turn | AI 回合 |
| end | 游戏结束 |
