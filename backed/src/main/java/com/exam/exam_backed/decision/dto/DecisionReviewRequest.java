package com.exam.exam_backed.decision.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record DecisionReviewRequest(
        @NotBlank(message = "满意度不能为空")
        @Pattern(regexp = "满意|一般|后悔", message = "满意度只能是满意、一般或后悔")
        String satisfaction,

        @NotBlank(message = "复盘反馈不能为空")
        String feedback,

        String betterChoice
) {
    public DecisionReviewRequest(String satisfaction, String feedback) {
        this(satisfaction, feedback, null);
    }
}
