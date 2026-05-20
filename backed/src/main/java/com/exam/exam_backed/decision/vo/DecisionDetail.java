package com.exam.exam_backed.decision.vo;

import java.time.LocalDateTime;
import java.util.List;

public record DecisionDetail(
        Long id,
        String title,
        String context,
        List<DecisionOption> options,
        String finalChoice,
        String reason,
        String status,
        LocalDateTime createTime
) {
}
