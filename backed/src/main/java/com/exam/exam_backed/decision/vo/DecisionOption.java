package com.exam.exam_backed.decision.vo;

import java.util.List;

public record DecisionOption(String id, String title, List<DecisionOption> children) {
}
