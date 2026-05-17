package com.exam.exam_backed.decision.vo;

import java.util.Map;

public record DecisionSummary(int total, int pending, int reviewed, Map<String, Integer> satisfaction) {
}
