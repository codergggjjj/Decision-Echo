package com.exam.exam_backed.goal.vo;

import java.time.LocalDateTime;

public record GoalDecisionVO(
        Long id,
        String title,
        String status,
        String finalChoice,
        LocalDateTime createTime
) {
}
