package com.exam.exam_backed.goal;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.exam.exam_backed.decision.Decision;
import com.exam.exam_backed.decision.DecisionGoal;
import com.exam.exam_backed.decision.mapper.DecisionGoalMapper;
import com.exam.exam_backed.decision.mapper.DecisionMapper;
import com.exam.exam_backed.goal.dto.GoalRequest;
import com.exam.exam_backed.goal.mapper.GoalMapper;
import com.exam.exam_backed.goal.service.GoalService;
import com.exam.exam_backed.goal.service.impl.GoalServiceImpl;
import com.exam.exam_backed.support.AbstractBaseMapperStub;
import com.exam.exam_backed.tag.GoalTag;
import com.exam.exam_backed.tag.Tag;
import com.exam.exam_backed.tag.mapper.GoalTagMapper;
import com.exam.exam_backed.tag.service.TagService;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GoalServiceImplTest {
    private final FakeGoalMapper goalMapper = new FakeGoalMapper();
    private final FakeGoalTagMapper goalTagMapper = new FakeGoalTagMapper();
    private final FakeDecisionMapper decisionMapper = new FakeDecisionMapper();
    private final FakeDecisionGoalMapper decisionGoalMapper = new FakeDecisionGoalMapper();
    private final FakeTagService tagService = new FakeTagService();
    private final GoalService goalService = new GoalServiceImpl(goalMapper, goalTagMapper, decisionMapper, tagService);

    @Test
    void createGoalStoresCurrentUserDefaultsAndBindsTags() {
        var detail = goalService.create(7L, new GoalRequest(
                "提升编程能力",
                "完成项目",
                "学习",
                null,
                null,
                LocalDate.of(2026, 12, 31),
                "完成 3 个项目",
                null,
                List.of("学习", " 项目 ", "学习")
        ));

        Goal stored = goalMapper.goals.get(0);
        assertEquals(7L, stored.userId());
        assertEquals("MEDIUM", stored.priority());
        assertEquals("IN_PROGRESS", stored.status());
        assertEquals(0, stored.progress());
        assertEquals("提升编程能力", detail.title());
        assertEquals(List.of("学习", "项目"), tagService.lastGoalTagNames);
        assertEquals(List.of(1L, 2L), tagService.boundGoalTagIds);
    }

    @Test
    void detailAggregatesGoalStatsAndDecisionList() {
        goalMapper.seed(new Goal(1L, 7L, "提升编程能力", "完成项目", "学习", "HIGH", "IN_PROGRESS",
                LocalDate.of(2026, 12, 31), "完成 3 个项目", 40, LocalDateTime.now(), LocalDateTime.now()));
        decisionMapper.decisions = List.of(
                new Decision(1L, 7L, 1L, "报名课程", "ctx", "报名,不报名", "reason", "学习", "平静", 2,
                        LocalDateTime.now(), "满意", "ok", "reviewed", 0, LocalDateTime.now(), LocalDateTime.now()),
                new Decision(2L, 7L, 1L, "做项目", "ctx", "做,不做", "reason", "学习", "平静", 2,
                        LocalDateTime.now(), null, null, "pending", 0, LocalDateTime.now(), LocalDateTime.now())
        );

        var detail = goalService.detail(7L, 1L);

        assertEquals(2, detail.decisionCount());
        assertEquals(1, detail.stats().reviewedCount());
        assertEquals(1, detail.stats().pendingReviewCount());
        assertEquals(1, detail.stats().satisfiedCount());
        assertEquals(List.of("报名课程", "做项目"), detail.decisions().stream().map(item -> item.title()).toList());
    }

    @Test
    void detailAggregatesDecisionGoalLinksAndLegacyGoalId() {
        GoalService service = new GoalServiceImpl(goalMapper, goalTagMapper, decisionMapper, decisionGoalMapper, tagService);
        goalMapper.seed(new Goal(1L, 7L, "提升编程能力", "完成项目", "学习", "HIGH", "IN_PROGRESS",
                LocalDate.of(2026, 12, 31), "完成 3 个项目", 40, LocalDateTime.now(), LocalDateTime.now()));
        decisionMapper.decisions = List.of(
                new Decision(1L, 7L, null, "关联表决策", "ctx", "做,不做", "reason", "学习", "平静", 2,
                        LocalDateTime.now(), null, null, "pending", 0, LocalDateTime.now(), LocalDateTime.now()),
                new Decision(2L, 7L, 1L, "旧字段决策", "ctx", "做,不做", "reason", "学习", "平静", 2,
                        LocalDateTime.now(), "满意", "ok", "reviewed", 0, LocalDateTime.now(), LocalDateTime.now())
        );
        decisionGoalMapper.goalLinks = List.of(new DecisionGoal(1L, 1L, 1L));

        var detail = service.detail(7L, 1L);

        assertEquals(2, detail.decisionCount());
        assertEquals(1, detail.stats().reviewedCount());
        assertEquals(1, detail.stats().pendingReviewCount());
        assertEquals(List.of("关联表决策", "旧字段决策"), detail.decisions().stream().map(item -> item.title()).toList());
    }

    @Test
    void recommendReturnsTopThreeByMatchedTagCount() {
        tagService.recommendTags = List.of(new Tag(1L, 7L, "学习", null, null), new Tag(2L, 7L, "项目", null, null));
        goalMapper.seed(new Goal(1L, 7L, "目标1", "", "学习", "HIGH", "IN_PROGRESS", null, "", 10, null, null));
        goalMapper.seed(new Goal(2L, 7L, "目标2", "", "学习", "HIGH", "IN_PROGRESS", null, "", 10, null, null));
        goalTagMapper.goalTags = List.of(
                new GoalTag(1L, 1L, 1L),
                new GoalTag(2L, 1L, 2L),
                new GoalTag(3L, 2L, 1L)
        );

        var recommendations = goalService.recommendGoalsByTags(7L, List.of("学习", "项目"));

        assertEquals(List.of("目标1", "目标2"), recommendations.stream().map(item -> item.title()).toList());
    }

    @Test
    void recommendTreatsConsumptionTagAsRelatedToFinanceGoalTag() {
        goalMapper.seed(new Goal(1L, 7L, "控制年度预算", "", "财务", "HIGH", "IN_PROGRESS", null, "", 10, null, null));
        goalTagMapper.goalTags = List.of(new GoalTag(1L, 1L, 2L));

        var recommendations = goalService.recommendGoalsByTags(7L, List.of("消费"));

        assertEquals(List.of("消费", "财务"), tagService.lastGoalTagNames);
        assertEquals(List.of("控制年度预算"), recommendations.stream().map(item -> item.title()).toList());
    }

    @Test
    void recommendTreatsConsumptionTagAsRelatedToFinanceGoalCategory() {
        goalMapper.seed(new Goal(1L, 7L, "控制年度预算", "", "财务", "HIGH", "IN_PROGRESS", null, "", 10, null, null));
        goalTagMapper.goalTags = List.of();

        var recommendations = goalService.recommendGoalsByTags(7L, List.of("消费"));

        assertEquals(List.of("控制年度预算"), recommendations.stream().map(item -> item.title()).toList());
    }

    @Test
    void deleteGoalClearsDecisionGoalIdAndRemovesGoalTags() {
        goalMapper.seed(new Goal(1L, 7L, "目标1", "", "学习", "HIGH", "IN_PROGRESS", null, "", 10, null, null));

        goalService.delete(7L, 1L);

        assertEquals(1L, decisionMapper.clearedGoalId);
        assertTrue(goalMapper.goals.isEmpty());
        assertEquals(1L, goalTagMapper.deletedGoalId);
    }

    private static class FakeGoalMapper extends AbstractBaseMapperStub<Goal> implements GoalMapper {
        private final AtomicLong idGenerator = new AtomicLong(1);
        private final List<Goal> goals = new ArrayList<>();

        void seed(Goal goal) {
            goals.add(goal);
        }

        @Override
        public int insert(Goal goal) {
            goal.setId(idGenerator.getAndIncrement());
            goals.add(goal);
            return 1;
        }

        @Override
        public Goal selectOne(Wrapper<Goal> queryWrapper) {
            return goals.isEmpty() ? null : goals.get(0);
        }

        @Override
        public List<Goal> selectList(Wrapper<Goal> queryWrapper) {
            return goals;
        }

        @Override
        public int updateById(Goal entity) {
            return 1;
        }

        @Override
        public int delete(Wrapper<Goal> queryWrapper) {
            goals.clear();
            return 1;
        }
    }

    private static class FakeGoalTagMapper extends AbstractBaseMapperStub<GoalTag> implements GoalTagMapper {
        private List<GoalTag> goalTags = new ArrayList<>();
        private Long deletedGoalId;

        @Override
        public List<GoalTag> selectList(Wrapper<GoalTag> queryWrapper) {
            return goalTags;
        }

        @Override
        public int delete(Wrapper<GoalTag> queryWrapper) {
            deletedGoalId = 1L;
            return 1;
        }
    }

    private static class FakeDecisionMapper extends AbstractBaseMapperStub<Decision> implements DecisionMapper {
        private List<Decision> decisions = new ArrayList<>();
        private Long clearedGoalId;

        @Override
        public List<Decision> selectList(Wrapper<Decision> queryWrapper) {
            return decisions;
        }

        @Override
        public Long selectCount(Wrapper<Decision> queryWrapper) {
            return (long) decisions.size();
        }

        @Override
        public int update(Decision entity, Wrapper<Decision> updateWrapper) {
            clearedGoalId = 1L;
            return 1;
        }

        @Override
        public int countReviewedBySatisfactionAndFilters(Long userId, String satisfaction, String tag, String mood) {
            throw unsupported();
        }

        @Override
        public List<MoodSatisfactionCount> countReviewedByMoodAndSatisfaction(Long userId) {
            throw unsupported();
        }

        @Override
        public List<TrendCount> countCreatedByMonth(Long userId, LocalDateTime start, LocalDateTime end) {
            throw unsupported();
        }

        @Override
        public List<TrendCount> countCreatedByDay(Long userId, LocalDateTime start, LocalDateTime end) {
            throw unsupported();
        }

        @Override
        public List<String> findTagsByUserId(Long userId) {
            throw unsupported();
        }
    }

    private static class FakeDecisionGoalMapper extends AbstractBaseMapperStub<DecisionGoal> implements DecisionGoalMapper {
        private List<DecisionGoal> goalLinks = new ArrayList<>();
        private Long deletedGoalId;

        @Override
        public List<DecisionGoal> selectList(Wrapper<DecisionGoal> queryWrapper) {
            return goalLinks;
        }

        @Override
        public int delete(Wrapper<DecisionGoal> queryWrapper) {
            deletedGoalId = 1L;
            return 1;
        }
    }

    private static class FakeTagService implements TagService {
        private List<String> lastGoalTagNames = List.of();
        private List<Long> boundGoalTagIds = List.of();
        private List<Tag> recommendTags = List.of();

        @Override
        public List<Tag> getOrCreateTags(Long userId, List<String> tagNames) {
            if (!recommendTags.isEmpty()) {
                return recommendTags;
            }
            lastGoalTagNames = tagNames.stream().map(String::trim).distinct().toList();
            List<Tag> tags = new ArrayList<>();
            for (int i = 0; i < lastGoalTagNames.size(); i++) {
                tags.add(new Tag((long) i + 1, userId, lastGoalTagNames.get(i), null, null));
            }
            return tags;
        }

        @Override
        public List<Tag> getOrCreateTags(Long userId, String rawTags) {
            return getOrCreateTags(userId, splitTagNames(rawTags));
        }

        @Override
        public void bindGoalTags(Long goalId, List<Long> tagIds) {
            boundGoalTagIds = tagIds;
        }

        @Override
        public void bindDecisionTags(Long decisionId, List<Long> tagIds) {
        }

        @Override
        public List<String> splitTagNames(String rawTags) {
            return rawTags == null ? List.of() : List.of(rawTags.split(","));
        }
    }
}
