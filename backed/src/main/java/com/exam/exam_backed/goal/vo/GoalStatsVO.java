package com.exam.exam_backed.goal.vo;

public record GoalStatsVO(
        Integer decisionCount,
        Integer reviewedCount,
        Integer pendingReviewCount,
        Integer satisfiedCount
) {
}
