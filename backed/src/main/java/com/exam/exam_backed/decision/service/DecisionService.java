package com.exam.exam_backed.decision.service;

import com.exam.exam_backed.decision.Decision;
import com.exam.exam_backed.decision.dto.DecisionCreateRequest;
import com.exam.exam_backed.decision.dto.DecisionReviewRequest;
import com.exam.exam_backed.decision.vo.DecisionDashboard;
import com.exam.exam_backed.decision.vo.DecisionSummary;

import java.util.List;

public interface DecisionService {
    Decision create(Long userId, DecisionCreateRequest request);

    List<Decision> recent(Long userId, int limit);

    List<Decision> search(Long userId, String keyword, String tag, String status, int limit);

    DecisionDashboard dashboard(Long userId);

    DecisionSummary summary(Long userId);

    Decision review(Long userId, Long decisionId, DecisionReviewRequest request);
}
