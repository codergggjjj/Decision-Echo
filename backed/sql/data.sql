USE `exam_final`;

INSERT INTO `user` (`id`, `username`, `password_hash`, `nickname`, `status`)
VALUES
    (1, 'test_user', '$2a$10$kJMUF688VDvD.Fwasn2J5.sdQppOqWjAbYVHuGOHca0cqnFdJ1Qm2', '测试用户', 1),
    (2, 'admin', '$2a$10$GcnwMcBBWZZbKTLeaPKa3.iloX.6WUSqmDUgpE.j2Y/mF9ilrSaYG', '管理员', 1),
    (3, 'disabled_user', '$2a$10$OYZUN0Ck2wGvch.vqjRwMuOQnaMKhdR4jgWxIQhs9BAyMfSqcIDIW', '禁用用户', 0)
ON DUPLICATE KEY UPDATE
    `password_hash` = VALUES(`password_hash`),
    `nickname` = VALUES(`nickname`),
    `status` = VALUES(`status`);

INSERT INTO `decision` (
    `id`, `user_id`, `title`, `context`, `options`, `reason`, `tags`, `mood`, `urgency`,
    `review_time`, `satisfaction`, `feedback`, `status`
)
VALUES
    (
        1, 1, '是否报名周末课程',
        '最近想提升项目能力，但担心周末时间被占满。',
        '报名,先自学,下个月再看',
        '课程有明确作业和反馈，适合短期建立节奏。',
        '学习', '平静', 2,
        DATE_ADD(NOW(), INTERVAL 7 DAY), NULL, NULL, 'pending'
    ),
    (
        2, 1, '是否更换通勤路线',
        '原路线稳定但耗时较久，新路线需要换乘一次。',
        '保持原路线,尝试新路线一周',
        '想用一周实际体验判断，而不是凭感觉决定。',
        '生活', '犹豫', 1,
        DATE_SUB(NOW(), INTERVAL 2 DAY), '满意', '新路线虽然多一次换乘，但整体节省了15分钟。', 'reviewed'
    ),
    (
        3, 1, '是否购买新耳机',
        '旧耳机还能用，但降噪效果不太够。',
        '暂不购买,购买入门款,购买高配款',
        '当前不是刚需，先观察一个月再决定。',
        '消费', '克制', 1,
        DATE_ADD(NOW(), INTERVAL 30 DAY), NULL, NULL, 'pending'
    ),
    (
        4, 1, '是否主动申请项目展示',
        '课程项目接近完成，展示会带来压力但也能获得反馈。',
        '申请展示,只提交文档',
        '希望通过展示发现问题，也锻炼表达。',
        '学习,表达', '紧张', 3,
        DATE_SUB(NOW(), INTERVAL 5 DAY), '一般', '展示过程有些紧张，但老师反馈很具体，值得做。', 'reviewed'
    ),
    (
        5, 1, '是否开始晚间跑步',
        '最近久坐偏多，希望用低门槛方式恢复运动。',
        '晚间跑步20分钟,先散步,周末集中运动',
        '每天20分钟更容易坚持，也能观察睡眠变化。',
        '健康', '期待', 2,
        DATE_SUB(NOW(), INTERVAL 1 HOUR), NULL, NULL, 'pending'
    ),
    (
        6, 1, '是否接一个临时项目',
        '项目报酬还可以，但会占用周末和复盘时间。',
        '接项目,拒绝项目,推迟一周再答复',
        '当时觉得可以提高收入，但忽略了时间成本。',
        '工作', '纠结', 3,
        DATE_SUB(NOW(), INTERVAL 8 DAY), '后悔', '实际占用了太多休息时间，影响了后续学习计划。', 'reviewed'
    ),
    (
        7, 1, '是否调整早餐习惯',
        '早上经常匆忙出门，早餐不稳定。',
        '提前准备,楼下购买,继续随缘',
        '提前准备能省时间，也更容易控制花费。',
        '生活', '平静', 1,
        DATE_SUB(NOW(), INTERVAL 2 HOUR), NULL, NULL, 'pending'
    )
ON DUPLICATE KEY UPDATE
    `title` = VALUES(`title`),
    `context` = VALUES(`context`),
    `options` = VALUES(`options`),
    `reason` = VALUES(`reason`),
    `tags` = VALUES(`tags`),
    `mood` = VALUES(`mood`),
    `urgency` = VALUES(`urgency`),
    `review_time` = VALUES(`review_time`),
    `satisfaction` = VALUES(`satisfaction`),
    `feedback` = VALUES(`feedback`),
    `status` = VALUES(`status`);
