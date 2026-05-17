package com.exam.exam_backed.decision.vo;

import com.exam.exam_backed.decision.Decision;

import java.util.List;

public record DecisionDashboard(DecisionSummary summary, List<Decision> recent, List<Decision> pendingReview) {
}
