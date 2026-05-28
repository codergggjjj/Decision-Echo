package com.exam.exam_backed.advice.dto;

import java.time.LocalDateTime;
import java.util.List;

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
        Long goalId,
        List<Long> goalIds
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
                reviewTime, satisfaction, feedback, historySummary, null, List.of());
    }

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
            String historySummary,
            Long goalId
    ) {
        this(decisionId, mode, title, context, options, selectedOption, reason, tags, mood, urgency,
                reviewTime, satisfaction, feedback, historySummary, goalId, List.of());
    }
}
