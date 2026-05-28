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
        String nextReminder,
        GoalAlignmentDTO goalAlignment
) {
    public DecisionAdviceResponse(
            String overallAdvice,
            List<OptionAdvice> options,
            String reminder,
            String summary,
            String factors,
            String risks,
            List<String> improvements,
            String nextReminder
    ) {
        this(overallAdvice, options, reminder, summary, factors, risks, improvements, nextReminder, null);
    }

    public record OptionAdvice(
            String name,
            List<String> pros,
            List<String> cons,
            List<String> risks,
            String bestFor,
            String suggestion
    ) {
    }

    public record GoalAlignmentDTO(
            Integer score,
            String level,
            String bestOption,
            String reason,
            List<OptionAlignmentDTO> optionAnalysis
    ) {
    }

    public record OptionAlignmentDTO(
            String option,
            Integer score,
            String comment
    ) {
    }
}
