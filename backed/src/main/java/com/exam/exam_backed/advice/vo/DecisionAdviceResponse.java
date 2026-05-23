package com.exam.exam_backed.advice.vo;

import java.util.List;

public record DecisionAdviceResponse(
        String overallAdvice,
        List<OptionAdvice> options,
        String reminder,
        String summary,
        String factors,
        String risks,
        List<String> improvements,
        String nextReminder
) {
    public record OptionAdvice(
            String name,
            List<String> pros,
            List<String> cons,
            List<String> risks,
            String bestFor,
            String suggestion
    ) {
    }
}
