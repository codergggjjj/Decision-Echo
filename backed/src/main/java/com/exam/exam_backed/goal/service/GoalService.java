package com.exam.exam_backed.goal.service;

import com.exam.exam_backed.decision.Decision;
import com.exam.exam_backed.goal.dto.GoalRequest;
import com.exam.exam_backed.goal.vo.GoalDetailVO;
import com.exam.exam_backed.goal.vo.GoalListItemVO;

import java.util.List;

public interface GoalService {
    GoalDetailVO create(Long userId, GoalRequest request);

    List<GoalListItemVO> list(Long userId, String status);

    GoalDetailVO detail(Long userId, Long goalId);

    GoalDetailVO update(Long userId, Long goalId, GoalRequest request);

    void delete(Long userId, Long goalId);

    List<GoalListItemVO> recommendGoalsByTags(Long userId, List<String> tags);

    List<Decision> decisionsByGoal(Long userId, Long goalId);
}
