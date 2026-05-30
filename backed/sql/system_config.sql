USE `exam_final`;

CREATE TABLE IF NOT EXISTS `system_config` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `config_key` VARCHAR(100) NOT NULL,
    `config_value` TEXT NOT NULL,
    `description` VARCHAR(255) DEFAULT NULL,
    `deleted` TINYINT NOT NULL DEFAULT 0,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_system_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='system configuration';

INSERT INTO `system_config` (`config_key`, `config_value`, `description`)
VALUES
    ('advice.ai.api_key', 'sk-bf8318519128484f98814f4a4adb4dc3', 'AI API Key'),
    ('advice.ai.base_url', 'https://dashscope.aliyuncs.com/compatible-mode/v1', 'AI service base URL'),
    ('advice.ai.model', 'qwen-plus', 'AI model name')
ON DUPLICATE KEY UPDATE
    `config_value` = IF(
        `config_key` = 'advice.ai.api_key' AND `config_value` <> '',
        `config_value`,
        VALUES(`config_value`)
    ),
    `description` = VALUES(`description`),
    `deleted` = 0;
