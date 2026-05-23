package com.exam.exam_backed.admin.vo;

public record AdminStatsVO(
        int userTotal,
        int decisionTotal,
        int reviewedTotal,
        int pendingTotal
) {
}
