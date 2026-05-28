package com.exam.exam_backed.decision.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

public record DecisionCreateRequest(
        @NotBlank(message = "标题不能为空")
        @Size(max = 100, message = "标题长度不能超过100")
        String title,

        String context,

        @NotBlank(message = "候选方案不能为空")
        String options,

        @NotBlank(message = "决策原因不能为空")
        String reason,

        @Size(max = 255, message = "标签长度不能超过255")
        String tags,

        @Size(max = 50, message = "心情长度不能超过50")
        String mood,

        @NotNull(message = "紧急度不能为空")
        @Min(value = 1, message = "紧急度最小为1")
        @Max(value = 3, message = "紧急度最大为3")
        Integer urgency,

        @NotNull(message = "回测时间不能为空")
        LocalDateTime reviewTime,

        Long goalId,

        List<Long> goalIds
) {
    public DecisionCreateRequest(
            String title,
            String context,
            String options,
            String reason,
            String tags,
            String mood,
            Integer urgency,
            LocalDateTime reviewTime
    ) {
        this(title, context, options, reason, tags, mood, urgency, reviewTime, null, List.of());
    }

    public DecisionCreateRequest(
            String title,
            String context,
            String options,
            String reason,
            String tags,
            String mood,
            Integer urgency,
            LocalDateTime reviewTime,
            Long goalId
    ) {
        this(title, context, options, reason, tags, mood, urgency, reviewTime, goalId, List.of());
    }
}
