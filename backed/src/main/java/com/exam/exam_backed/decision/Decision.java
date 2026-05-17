package com.exam.exam_backed.decision;

import java.time.LocalDateTime;

public record Decision(
        Long id,
        Long userId,
        String title,
        String context,
        String options,
        String reason,
        String tags,
        String mood,
        Integer urgency,
        LocalDateTime reviewTime,
        String satisfaction,
        String feedback,
        String status,
        LocalDateTime createTime,
        LocalDateTime updateTime
) {
    public Decision withId(Long newId) {
        return new Decision(newId, userId, title, context, options, reason, tags, mood, urgency, reviewTime,
                satisfaction, feedback, status, createTime, updateTime);
    }

    public Decision withReview(String newSatisfaction, String newFeedback, String newStatus) {
        return new Decision(id, userId, title, context, options, reason, tags, mood, urgency, reviewTime,
                newSatisfaction, newFeedback, newStatus, createTime, LocalDateTime.now());
    }
}
