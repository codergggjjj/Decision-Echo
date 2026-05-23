package com.exam.exam_backed.advice.dto;

import java.time.LocalDateTime;

public record AdviceGenerateRequest(
        String mode,
        String title,
        String context,
        String options,
        String selectedOption,
        String reason,
        String tags,
        String mood,
        Integer urgency,
        LocalDateTime reviewTime,
        String satisfaction,
        String feedback,
        String historySummary
) {
}
