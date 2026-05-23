package com.exam.exam_backed.admin.vo;

import java.time.LocalDateTime;

public record AdminDecisionVO(
        Long id,
        Long userId,
        String username,
        String title,
        String tags,
        String mood,
        Integer urgency,
        LocalDateTime reviewTime,
        String satisfaction,
        String status,
        LocalDateTime createTime
) {
}
