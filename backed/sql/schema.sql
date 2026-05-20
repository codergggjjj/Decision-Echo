CREATE DATABASE IF NOT EXISTS `exam_final`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE `exam_final`;

CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '登录用户名',
    `password_hash` VARCHAR(100) NOT NULL COMMENT 'BCrypt密码哈希',
    `nickname` VARCHAR(50) NOT NULL COMMENT '用户昵称',
    `avatar_url` VARCHAR(500) DEFAULT NULL COMMENT '头像地址',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1启用，0禁用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

ALTER TABLE `user`
    ADD COLUMN IF NOT EXISTS `avatar_url` VARCHAR(500) DEFAULT NULL COMMENT '头像地址' AFTER `nickname`;

CREATE TABLE IF NOT EXISTS `decision` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '决策ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
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
    CONSTRAINT `fk_decision_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='决策记录表';
