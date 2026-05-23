package com.exam.exam_backed.decision.vo;

import java.util.List;

public record DecisionOption(String id, String title, String strategy, List<DecisionOption> children) {
    public DecisionOption(String id, String title, List<DecisionOption> children) {
        this(id, title, "", children);
    }
}
