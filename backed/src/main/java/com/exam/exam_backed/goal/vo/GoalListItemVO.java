package com.exam.exam_backed.goal.vo;

import java.time.LocalDate;

public record GoalListItemVO(
        Long id,
        String title,
        String description,
        String category,
        String priority,
        String status,
        LocalDate targetDate,
        String measurement,
        Integer progress,
        Integer decisionCount
) {
}
