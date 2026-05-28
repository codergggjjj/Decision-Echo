package com.exam.exam_backed.decision.vo;

import java.time.LocalDateTime;
import java.util.List;

public record DecisionDetail(
        Long id,
        Long goalId,
        String goalTitle,
        List<Long> goalIds,
        List<DecisionGoalVO> goals,
        String title,
        String context,
        List<DecisionOption> options,
        String finalChoice,
        String reason,
        String satisfaction,
        String feedback,
        String status,
        LocalDateTime createTime
) {
    public DecisionDetail(
            Long id,
            String title,
            String context,
            List<DecisionOption> options,
            String finalChoice,
            String reason,
            String satisfaction,
            String feedback,
            String status,
            LocalDateTime createTime
    ) {
        this(id, null, null, List.of(), List.of(), title, context, options, finalChoice, reason, satisfaction, feedback, status, createTime);
    }
}
