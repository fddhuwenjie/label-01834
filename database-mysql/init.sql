CREATE DATABASE IF NOT EXISTS mahjong_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE mahjong_db;

CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `nickname` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户昵称',
    `password` VARCHAR(255) NOT NULL COMMENT 'BCrypt加密密码',
    `avatar` VARCHAR(255) DEFAULT '' COMMENT '头像URL',
    `total_games` INT DEFAULT 0 COMMENT '总局数',
    `win_games` INT DEFAULT 0 COMMENT '胜局数',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `game_record` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL COMMENT '玩家ID',
    `result` VARCHAR(10) NOT NULL COMMENT 'win/lose/draw',
    `rounds` INT DEFAULT 0 COMMENT '回合数',
    `score` INT DEFAULT 0 COMMENT '得分',
    `duration` INT DEFAULT 0 COMMENT '时长(秒)',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_user_id` (`user_id`),
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `game_setting` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL COMMENT '玩家ID',
    `setting_key` VARCHAR(50) NOT NULL COMMENT '设置键',
    `setting_value` VARCHAR(255) NOT NULL COMMENT '设置值',
    UNIQUE KEY `uk_user_key` (`user_id`, `setting_key`),
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 测试账号 (BCrypt 加密，应用启动时 DataInitializer 会确保账号存在并校正密码哈希)
-- admin / admin123
INSERT INTO `user` (`nickname`, `password`, `avatar`, `total_games`, `win_games`, `created_at`, `updated_at`)
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '', 15, 8, NOW(), NOW())
ON DUPLICATE KEY UPDATE `nickname` = `nickname`;

-- user / user123
INSERT INTO `user` (`nickname`, `password`, `avatar`, `total_games`, `win_games`, `created_at`, `updated_at`)
VALUES ('user', '$2a$10$wGXFhLLuGHhTPUJVYMHiXuI0x.mh1i7ZgkZqGHvE3Ht0E3l8TxEya', '', 10, 3, NOW(), NOW())
ON DUPLICATE KEY UPDATE `nickname` = `nickname`;

-- 示例游戏记录 (admin 用户)
INSERT INTO `game_record` (`user_id`, `result`, `rounds`, `score`, `duration`, `created_at`) VALUES
(1, 'win', 32, 10, 480, '2026-02-25 14:30:00'),
(1, 'lose', 45, -5, 620, '2026-02-25 15:10:00'),
(1, 'win', 28, 10, 390, '2026-02-26 09:20:00'),
(1, 'draw', 68, 0, 900, '2026-02-26 10:45:00'),
(1, 'win', 35, 10, 510, '2026-02-27 16:00:00'),
(1, 'lose', 50, -5, 720, '2026-02-27 17:30:00'),
(1, 'win', 22, 10, 300, '2026-02-28 11:00:00'),
(1, 'win', 40, 10, 560, '2026-02-28 14:20:00');

-- 示例游戏记录 (user 用户)
INSERT INTO `game_record` (`user_id`, `result`, `rounds`, `score`, `duration`, `created_at`) VALUES
(2, 'lose', 55, -5, 780, '2026-02-25 13:00:00'),
(2, 'win', 30, 10, 420, '2026-02-26 08:30:00'),
(2, 'lose', 42, -5, 600, '2026-02-26 15:00:00'),
(2, 'draw', 68, 0, 910, '2026-02-27 10:00:00'),
(2, 'win', 25, 10, 350, '2026-02-27 19:00:00'),
(2, 'lose', 60, -5, 850, '2026-02-28 09:30:00');

-- 默认设置
INSERT INTO `game_setting` (`user_id`, `setting_key`, `setting_value`) VALUES
(1, 'ai_speed', 'normal'),
(1, 'sound_enabled', 'true'),
(1, 'auto_sort', 'true'),
(1, 'theme', 'classic'),
(2, 'ai_speed', 'fast'),
(2, 'sound_enabled', 'true'),
(2, 'auto_sort', 'true'),
(2, 'theme', 'classic');
