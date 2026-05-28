package com.exam.exam_backed.goal.vo;

import java.time.LocalDate;
import java.util.List;

public record GoalDetailVO(
        Long id,
        String title,
        String description,
        String category,
        String priority,
        String status,
        LocalDate targetDate,
        String measurement,
        Integer progress,
        Integer decisionCount,
        GoalStatsVO stats,
        List<GoalDecisionVO> decisions
) {
}
