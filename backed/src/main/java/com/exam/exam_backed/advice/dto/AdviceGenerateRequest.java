package com.exam.exam_backed.advice.dto;

import java.time.LocalDateTime;

public record AdviceGenerateRequest(
        Long decisionId,
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
        String historySummary,
        Long goalId
) {
    public AdviceGenerateRequest(
            Long decisionId,
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
        this(decisionId, mode, title, context, options, selectedOption, reason, tags, mood, urgency,
                reviewTime, satisfaction, feedback, historySummary, null);
    }
}
