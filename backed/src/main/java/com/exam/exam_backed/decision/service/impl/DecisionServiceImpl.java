package com.exam.exam_backed.decision.service.impl;

import com.exam.exam_backed.common.BusinessException;
import com.exam.exam_backed.common.ErrorCode;
import com.exam.exam_backed.decision.Decision;
import com.exam.exam_backed.decision.dto.DecisionCreateRequest;
import com.exam.exam_backed.decision.dto.DecisionReviewRequest;
import com.exam.exam_backed.decision.mapper.DecisionMapper;
import com.exam.exam_backed.decision.service.DecisionService;
import com.exam.exam_backed.decision.vo.DecisionDashboard;
import com.exam.exam_backed.decision.vo.DecisionSummary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DecisionServiceImpl implements DecisionService {
    private static final String STATUS_PENDING = "pending";
    private static final String STATUS_REVIEWED = "reviewed";
    private final DecisionMapper decisionMapper;

    public DecisionServiceImpl(DecisionMapper decisionMapper) {
        this.decisionMapper = decisionMapper;
    }

    @Override
    @Transactional
    public Decision create(Long userId, DecisionCreateRequest request) {
        Decision decision = new Decision(
                null,
                userId,
                request.title(),
                request.context() == null ? "" : request.context(),
                request.options(),
                request.reason(),
                request.tags(),
                request.mood(),
                request.urgency(),
                request.reviewTime(),
                null,
                null,
                STATUS_PENDING,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        decisionMapper.insert(decision);
        Long id = decisionMapper.lastInsertedId();
        return decisionMapper.findByIdAndUserId(id, userId).orElse(decision.withId(id));
    }

    @Override
    public List<Decision> recent(Long userId, int limit) {
        int normalizedLimit = Math.min(Math.max(limit, 1), 20);
        return decisionMapper.findRecentByUserId(userId, normalizedLimit);
    }

    @Override
    public DecisionDashboard dashboard(Long userId) {
        List<Decision> recent = recent(userId, 20);
        List<Decision> pendingReview = decisionMapper.findDuePendingReviewByUserId(userId, 5);
        return new DecisionDashboard(summary(userId), recent, pendingReview);
    }

    @Override
    public DecisionSummary summary(Long userId) {
        Map<String, Integer> satisfaction = new LinkedHashMap<>();
        satisfaction.put("满意", decisionMapper.countReviewedBySatisfaction(userId, "满意"));
        satisfaction.put("一般", decisionMapper.countReviewedBySatisfaction(userId, "一般"));
        satisfaction.put("后悔", decisionMapper.countReviewedBySatisfaction(userId, "后悔"));
        return new DecisionSummary(
                decisionMapper.countByUserId(userId),
                decisionMapper.countByUserIdAndStatus(userId, STATUS_PENDING),
                decisionMapper.countByUserIdAndStatus(userId, STATUS_REVIEWED),
                satisfaction
        );
    }

    @Override
    @Transactional
    public Decision review(Long userId, Long decisionId, DecisionReviewRequest request) {
        decisionMapper.findByIdAndUserId(decisionId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PARAM_ERROR, "决策记录不存在"));
        decisionMapper.updateReview(decisionId, userId, request.satisfaction(), request.feedback(), STATUS_REVIEWED);
        return decisionMapper.findByIdAndUserId(decisionId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PARAM_ERROR, "决策记录不存在"));
    }
}
