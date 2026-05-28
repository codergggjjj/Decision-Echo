CREATE DATABASE IF NOT EXISTS `exam_final`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE `exam_final`;

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '登录用户名',
    `password_hash` VARCHAR(100) NOT NULL COMMENT 'BCrypt密码哈希',
    `nickname` VARCHAR(50) NOT NULL COMMENT '用户昵称',
    `avatar_url` VARCHAR(500) DEFAULT NULL COMMENT '头像地址',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1启用，0禁用',
    `role` VARCHAR(20) NOT NULL DEFAULT 'user' COMMENT '角色：admin管理员，user普通用户',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

DROP PROCEDURE IF EXISTS add_column_if_missing;

DELIMITER //
CREATE PROCEDURE add_column_if_missing(
    IN table_name_value VARCHAR(64),
    IN column_name_value VARCHAR(64),
    IN ddl_value TEXT
)
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = table_name_value
          AND COLUMN_NAME = column_name_value
    ) THEN
        SET @ddl_sql = ddl_value;
        PREPARE ddl_stmt FROM @ddl_sql;
        EXECUTE ddl_stmt;
        DEALLOCATE PREPARE ddl_stmt;
    END IF;
END//
DELIMITER ;

CALL add_column_if_missing('user', 'avatar_url', 'ALTER TABLE `user` ADD COLUMN `avatar_url` VARCHAR(500) DEFAULT NULL COMMENT ''头像地址'' AFTER `nickname`');

CALL add_column_if_missing('user', 'role', 'ALTER TABLE `user` ADD COLUMN `role` VARCHAR(20) NOT NULL DEFAULT ''user'' COMMENT ''角色：admin管理员，user普通用户'' AFTER `status`');

UPDATE `user`
SET `role` = 'admin'
WHERE `username` = 'admin';

CREATE TABLE IF NOT EXISTS `decision` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '决策ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `goal_id` BIGINT DEFAULT NULL COMMENT '关联的长期目标ID',
    `title` VARCHAR(100) NOT NULL COMMENT '决策标题',
    `context` TEXT NOT NULL COMMENT '决策背景',
    `options` TEXT NOT NULL COMMENT '候选方案，逗号或JSON文本',
    `reason` TEXT NOT NULL COMMENT '做出决策的原因',
    `tags` VARCHAR(255) DEFAULT NULL COMMENT '标签',
    `mood` VARCHAR(50) DEFAULT NULL COMMENT '决策时心情',
    `urgency` TINYINT NOT NULL DEFAULT 2 COMMENT '紧急度：1低，2中，3高',
    `review_time` DATETIME NOT NULL COMMENT '计划回测时间',
    `satisfaction` VARCHAR(20) DEFAULT NULL COMMENT '满意度：满意、一般、后悔',
    `feedback` TEXT DEFAULT NULL COMMENT '回测反馈',
    `status` VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '状态：pending待回测，reviewed已回测',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_decision_user_create_time` (`user_id`, `create_time`),
    KEY `idx_decision_user_review_time` (`user_id`, `review_time`),
    KEY `idx_decision_goal` (`goal_id`),
    CONSTRAINT `fk_decision_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='决策记录表';

CALL add_column_if_missing('decision', 'goal_id', 'ALTER TABLE `decision` ADD COLUMN `goal_id` BIGINT DEFAULT NULL COMMENT ''关联的长期目标ID'' AFTER `user_id`');

DROP PROCEDURE IF EXISTS add_index_if_missing;

DELIMITER //
CREATE PROCEDURE add_index_if_missing(
    IN table_name_value VARCHAR(64),
    IN index_name_value VARCHAR(64),
    IN ddl_value TEXT
)
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = table_name_value
          AND INDEX_NAME = index_name_value
    ) THEN
        SET @idx_sql = ddl_value;
        PREPARE idx_stmt FROM @idx_sql;
        EXECUTE idx_stmt;
        DEALLOCATE PREPARE idx_stmt;
    END IF;
END//
DELIMITER ;

CALL add_index_if_missing('decision', 'idx_decision_goal', 'ALTER TABLE `decision` ADD KEY `idx_decision_goal` (`goal_id`)');

CREATE TABLE IF NOT EXISTS `system_config` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '配置ID',
    `config_key` VARCHAR(100) NOT NULL COMMENT '配置键',
    `config_value` TEXT NOT NULL COMMENT '配置值',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '配置说明',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_system_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

INSERT INTO `system_config` (`config_key`, `config_value`, `description`)
VALUES
    ('advice.ai.base_url', 'https://dashscope.aliyuncs.com/compatible-mode/v1', 'AI service base URL'),
    ('advice.ai.model', 'qwen-plus', 'AI model name')
ON DUPLICATE KEY UPDATE
    `config_value` = VALUES(`config_value`),
    `description` = VALUES(`description`),
    `deleted` = 0;

CREATE TABLE IF NOT EXISTS `decision_goal` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '决策目标关系ID',
    `decision_id` BIGINT NOT NULL COMMENT '决策ID',
    `goal_id` BIGINT NOT NULL COMMENT '目标ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_decision_goal` (`decision_id`, `goal_id`),
    KEY `idx_decision_goal_goal_id` (`goal_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='决策目标关系表';

INSERT IGNORE INTO `decision_goal` (`decision_id`, `goal_id`)
SELECT `id`, `goal_id`
FROM `decision`
WHERE `goal_id` IS NOT NULL;

CREATE TABLE IF NOT EXISTS `goal` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '目标ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `title` VARCHAR(100) NOT NULL COMMENT '目标标题',
    `description` TEXT DEFAULT NULL COMMENT '目标描述',
    `category` VARCHAR(50) DEFAULT NULL COMMENT '目标分类',
    `priority` VARCHAR(20) NOT NULL DEFAULT 'MEDIUM' COMMENT '优先级：HIGH、MEDIUM、LOW',
    `status` VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS' COMMENT '状态：IN_PROGRESS、COMPLETED、ABANDONED',
    `target_date` DATE DEFAULT NULL COMMENT '预期完成日期',
    `measurement` VARCHAR(255) DEFAULT NULL COMMENT '衡量方式',
    `progress` INT NOT NULL DEFAULT 0 COMMENT '目标进度',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_goal_user_status` (`user_id`, `status`),
    KEY `idx_goal_user_target_date` (`user_id`, `target_date`),
    CONSTRAINT `fk_goal_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='长期目标表';

CREATE TABLE IF NOT EXISTS `tag` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '标签ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `name` VARCHAR(50) NOT NULL COMMENT '标签名称',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tag_user_name` (`user_id`, `name`),
    CONSTRAINT `fk_tag_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='标签表';

CREATE TABLE IF NOT EXISTS `goal_tag` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '目标标签关系ID',
    `goal_id` BIGINT NOT NULL COMMENT '目标ID',
    `tag_id` BIGINT NOT NULL COMMENT '标签ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_goal_tag` (`goal_id`, `tag_id`),
    KEY `idx_goal_tag_tag_id` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='目标标签关系表';

CREATE TABLE IF NOT EXISTS `decision_tag` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '决策标签关系ID',
    `decision_id` BIGINT NOT NULL COMMENT '决策ID',
    `tag_id` BIGINT NOT NULL COMMENT '标签ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_decision_tag` (`decision_id`, `tag_id`),
    KEY `idx_decision_tag_tag_id` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='决策标签关系表';
