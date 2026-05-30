package com.exam.exam_backed.goal.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.exam.exam_backed.common.BusinessException;
import com.exam.exam_backed.common.ErrorCode;
import com.exam.exam_backed.decision.Decision;
import com.exam.exam_backed.decision.DecisionGoal;
import com.exam.exam_backed.decision.mapper.DecisionGoalMapper;
import com.exam.exam_backed.decision.mapper.DecisionMapper;
import com.exam.exam_backed.goal.Goal;
import com.exam.exam_backed.goal.dto.GoalRequest;
import com.exam.exam_backed.goal.mapper.GoalMapper;
import com.exam.exam_backed.goal.service.GoalService;
import com.exam.exam_backed.goal.vo.GoalDecisionVO;
import com.exam.exam_backed.goal.vo.GoalDetailVO;
import com.exam.exam_backed.goal.vo.GoalListItemVO;
import com.exam.exam_backed.goal.vo.GoalStatsVO;
import com.exam.exam_backed.tag.GoalTag;
import com.exam.exam_backed.tag.Tag;
import com.exam.exam_backed.tag.mapper.GoalTagMapper;
import com.exam.exam_backed.tag.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class GoalServiceImpl implements GoalService {
    private static final String STATUS_IN_PROGRESS = "IN_PROGRESS";
    private static final Set<String> ALLOWED_STATUS = Set.of("IN_PROGRESS", "COMPLETED", "ABANDONED");
    private static final Set<String> ALLOWED_PRIORITY = Set.of("HIGH", "MEDIUM", "LOW");
    private static final String GOAL_NOT_FOUND = "长期目标不存在";

    private final GoalMapper goalMapper;
    private final GoalTagMapper goalTagMapper;
    private final DecisionMapper decisionMapper;
    private final DecisionGoalMapper decisionGoalMapper;
    private final TagService tagService;

    public GoalServiceImpl(GoalMapper goalMapper, GoalTagMapper goalTagMapper, DecisionMapper decisionMapper, TagService tagService) {
        this(goalMapper, goalTagMapper, decisionMapper, null, tagService);
    }

    @Autowired
    public GoalServiceImpl(GoalMapper goalMapper, GoalTagMapper goalTagMapper, DecisionMapper decisionMapper, DecisionGoalMapper decisionGoalMapper, TagService tagService) {
        this.goalMapper = goalMapper;
        this.goalTagMapper = goalTagMapper;
        this.decisionMapper = decisionMapper;
        this.decisionGoalMapper = decisionGoalMapper;
        this.tagService = tagService;
    }

    @Override
    @Transactional
    public GoalDetailVO create(Long userId, GoalRequest request) {
        Goal goal = new Goal();
        goal.setUserId(userId);
        goal.setTitle(requiredTitle(request.title()));
        applyRequest(goal, request);
        goal.setCreatedAt(LocalDateTime.now());
        goal.setUpdatedAt(LocalDateTime.now());
        goalMapper.insert(goal);
        bindTags(userId, goal.getId(), request.tags());
        return detail(userId, goal.getId());
    }

    @Override
    public List<GoalListItemVO> list(Long userId, String status) {
        String normalizedStatus = normalizeStatusFilter(status);
        LambdaQueryWrapper<Goal> query = new LambdaQueryWrapper<Goal>()
                .eq(Goal::getUserId, userId)
                .eq(normalizedStatus != null, Goal::getStatus, normalizedStatus)
                .orderByDesc(Goal::getUpdatedAt)
                .orderByDesc(Goal::getId);
        return goalMapper.selectList(query).stream()
                .map(goal -> toListItem(goal, decisionCount(userId, goal.getId())))
                .toList();
    }

    @Override
    public GoalDetailVO detail(Long userId, Long goalId) {
        Goal goal = findExistingGoal(userId, goalId);
        List<Decision> decisions = decisionsByGoal(userId, goalId);
        GoalStatsVO stats = stats(decisions);
        return new GoalDetailVO(
                goal.getId(),
                goal.getTitle(),
                goal.getDescription(),
                goal.getCategory(),
                goal.getPriority(),
                goal.getStatus(),
                goal.getTargetDate(),
                goal.getMeasurement(),
                goal.getProgress(),
                stats.decisionCount(),
                stats,
                decisions.stream().map(this::toGoalDecision).toList()
        );
    }

    @Override
    @Transactional
    public GoalDetailVO update(Long userId, Long goalId, GoalRequest request) {
        Goal goal = findExistingGoal(userId, goalId);
        goal.setTitle(requiredTitle(request.title()));
        applyRequest(goal, request);
        goal.setUpdatedAt(LocalDateTime.now());
        goalMapper.updateById(goal);
        if (request.tags() != null) {
            bindTags(userId, goalId, request.tags());
        }
        return detail(userId, goalId);
    }

    @Override
    @Transactional
    public void delete(Long userId, Long goalId) {
        findExistingGoal(userId, goalId);
        decisionMapper.update(null, new LambdaUpdateWrapper<Decision>()
                .eq(Decision::getUserId, userId)
                .eq(Decision::getGoalId, goalId)
                .eq(Decision::getDeleted, 0)
                .setSql("goal_id = NULL")
                .setSql("update_time = NOW()"));
        goalTagMapper.delete(new LambdaQueryWrapper<GoalTag>().eq(GoalTag::getGoalId, goalId));
        if (decisionGoalMapper != null) {
            decisionGoalMapper.delete(new LambdaQueryWrapper<DecisionGoal>().eq(DecisionGoal::getGoalId, goalId));
        }
        goalMapper.delete(new LambdaQueryWrapper<Goal>()
                .eq(Goal::getId, goalId)
                .eq(Goal::getUserId, userId));
    }

    @Override
    public List<GoalListItemVO> recommendGoalsByTags(Long userId, List<String> tags) {
        List<String> recommendationTags = expandRecommendationTags(tags);
        if (recommendationTags.isEmpty()) {
            return List.of();
        }
        List<Tag> tagList = tagService.getOrCreateTags(userId, recommendationTags);
        List<Long> tagIds = tagList.stream().map(Tag::getId).toList();
        List<GoalTag> goalTags = tagIds.isEmpty()
                ? List.of()
                : goalTagMapper.selectList(new LambdaQueryWrapper<GoalTag>().in(GoalTag::getTagId, tagIds));
        Map<Long, Long> matchCounts = goalTags.stream()
                .collect(Collectors.groupingBy(GoalTag::getGoalId, LinkedHashMap::new, Collectors.counting()));
        List<Long> candidateIds = new ArrayList<>(matchCounts.keySet());
        LambdaQueryWrapper<Goal> query = new LambdaQueryWrapper<Goal>()
                .eq(Goal::getUserId, userId)
                .eq(Goal::getStatus, STATUS_IN_PROGRESS);
        if (candidateIds.isEmpty()) {
            query.in(Goal::getCategory, recommendationTags);
        } else {
            query.and(wrapper -> wrapper.in(Goal::getId, candidateIds).or().in(Goal::getCategory, recommendationTags));
        }
        List<Goal> goals = goalMapper.selectList(query);
        return goals.stream()
                .sorted(Comparator
                        .comparingLong((Goal goal) -> recommendationScore(goal, matchCounts, recommendationTags)).reversed()
                        .thenComparing(Goal::getId))
                .limit(3)
                .map(goal -> toListItem(goal, decisionCount(userId, goal.getId())))
                .toList();
    }

    private long recommendationScore(Goal goal, Map<Long, Long> matchCounts, List<String> recommendationTags) {
        long score = matchCounts.getOrDefault(goal.getId(), 0L);
        if (goal.getCategory() != null && recommendationTags.contains(goal.getCategory())) {
            score += 1;
        }
        return score;
    }

    private List<String> expandRecommendationTags(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return List.of();
        }
        LinkedHashSet<String> expanded = new LinkedHashSet<>();
        for (String tag : tags) {
            if (tag == null || tag.trim().isBlank()) {
                continue;
            }
            String normalized = tag.trim();
            expanded.add(normalized);
            if ("消费".equals(normalized) || "财务".equals(normalized)) {
                expanded.add("消费");
                expanded.add("财务");
            }
        }
        return new ArrayList<>(expanded);
    }

    @Override
    public List<Decision> decisionsByGoal(Long userId, Long goalId) {
        findExistingGoal(userId, goalId);
        List<Long> decisionIds = decisionIdsByGoal(goalId);
        LambdaQueryWrapper<Decision> query = new LambdaQueryWrapper<Decision>()
                .eq(Decision::getUserId, userId)
                .eq(Decision::getDeleted, 0);
        if (decisionIds.isEmpty()) {
            query.eq(Decision::getGoalId, goalId);
        } else {
            query.and(wrapper -> wrapper.in(Decision::getId, decisionIds).or().eq(Decision::getGoalId, goalId));
        }
        return decisionMapper.selectList(query
                .orderByDesc(Decision::getCreateTime)
                .orderByDesc(Decision::getId));
    }

    private void applyRequest(Goal goal, GoalRequest request) {
        goal.setDescription(blankToNull(request.description()));
        goal.setCategory(blankToNull(request.category()));
        goal.setPriority(normalizePriority(request.priority()));
        goal.setStatus(normalizeStatus(request.status()));
        goal.setTargetDate(request.targetDate());
        goal.setMeasurement(blankToNull(request.measurement()));
        goal.setProgress(normalizeProgress(request.progress()));
    }

    private void bindTags(Long userId, Long goalId, List<String> tags) {
        List<Tag> tagList = tagService.getOrCreateTags(userId, tags);
        tagService.bindGoalTags(goalId, tagList.stream().map(Tag::getId).toList());
    }

    private Goal findExistingGoal(Long userId, Long goalId) {
        Goal goal = goalMapper.selectOne(new LambdaQueryWrapper<Goal>()
                .eq(Goal::getId, goalId)
                .eq(Goal::getUserId, userId)
                .last("LIMIT 1"));
        if (goal == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, GOAL_NOT_FOUND);
        }
        return goal;
    }

    private Integer decisionCount(Long userId, Long goalId) {
        return decisionsByGoal(userId, goalId).size();
    }

    private List<Long> decisionIdsByGoal(Long goalId) {
        if (decisionGoalMapper == null) {
            return List.of();
        }
        try {
            return decisionGoalMapper.selectList(new LambdaQueryWrapper<DecisionGoal>()
                            .eq(DecisionGoal::getGoalId, goalId))
                    .stream()
                    .map(DecisionGoal::getDecisionId)
                    .distinct()
                    .toList();
        } catch (DataAccessException exception) {
            return List.of();
        }
    }

    private GoalStatsVO stats(List<Decision> decisions) {
        int reviewed = 0;
        int pending = 0;
        int satisfied = 0;
        for (Decision decision : decisions) {
            if ("reviewed".equals(decision.getStatus())) {
                reviewed++;
            }
            if ("pending".equals(decision.getStatus())) {
                pending++;
            }
            if ("满意".equals(decision.getSatisfaction())) {
                satisfied++;
            }
        }
        return new GoalStatsVO(decisions.size(), reviewed, pending, satisfied);
    }

    private GoalListItemVO toListItem(Goal goal, Integer decisionCount) {
        return new GoalListItemVO(
                goal.getId(),
                goal.getTitle(),
                goal.getDescription(),
                goal.getCategory(),
                goal.getPriority(),
                goal.getStatus(),
                goal.getTargetDate(),
                goal.getMeasurement(),
                goal.getProgress(),
                decisionCount
        );
    }

    private GoalDecisionVO toGoalDecision(Decision decision) {
        return new GoalDecisionVO(
                decision.getId(),
                decision.getTitle(),
                decision.getStatus(),
                finalChoice(decision.getOptions()),
                decision.getCreateTime()
        );
    }

    private String finalChoice(String rawOptions) {
        if (rawOptions == null || rawOptions.isBlank()) {
            return "";
        }
        String selectedId = extractStringField(rawOptions, "selectedId");
        if (!selectedId.isBlank()) {
            int selectedAt = rawOptions.indexOf("\"id\":\"" + selectedId + "\"");
            if (selectedAt >= 0) {
                String selectedObject = rawOptions.substring(selectedAt);
                String title = extractStringField(selectedObject, "title");
                if (!title.isBlank()) {
                    return title;
                }
            }
        }
        return rawOptions.split(",", 2)[0].trim();
    }

    private String extractStringField(String rawObject, String fieldName) {
        int fieldStart = rawObject.indexOf("\"" + fieldName + "\"");
        if (fieldStart < 0) {
            return "";
        }
        int colon = rawObject.indexOf(':', fieldStart);
        int quoteStart = rawObject.indexOf('"', colon + 1);
        if (colon < 0 || quoteStart < 0) {
            return "";
        }
        int quoteEnd = rawObject.indexOf('"', quoteStart + 1);
        return quoteEnd < 0 ? "" : rawObject.substring(quoteStart + 1, quoteEnd);
    }

    private String requiredTitle(String title) {
        if (title == null || title.trim().isBlank()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "目标标题不能为空");
        }
        return title.trim();
    }

    private String normalizeStatusFilter(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        return normalizeStatus(status);
    }

    private String normalizeStatus(String status) {
        String value = status == null || status.isBlank() ? STATUS_IN_PROGRESS : status.trim();
        if (!ALLOWED_STATUS.contains(value)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "目标状态不正确");
        }
        return value;
    }

    private String normalizePriority(String priority) {
        String value = priority == null || priority.isBlank() ? "MEDIUM" : priority.trim();
        if (!ALLOWED_PRIORITY.contains(value)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "目标优先级不正确");
        }
        return value;
    }

    private Integer normalizeProgress(Integer progress) {
        if (progress == null) {
            return 0;
        }
        return Math.min(100, Math.max(0, progress));
    }

    private String blankToNull(String value) {
        return value == null || value.trim().isBlank() ? null : value.trim();
    }
}
