package com.exam.exam_backed.decision;

import com.exam.exam_backed.common.BusinessException;
import com.exam.exam_backed.analysis.service.impl.AnalysisServiceImpl;
import com.exam.exam_backed.decision.dto.DecisionCreateRequest;
import com.exam.exam_backed.decision.dto.DecisionReviewRequest;
import com.exam.exam_backed.decision.mapper.DecisionMapper;
import com.exam.exam_backed.decision.service.DecisionService;
import com.exam.exam_backed.decision.service.impl.DecisionServiceImpl;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class DecisionServiceImplTest {
    private final FakeDecisionMapper decisionMapper = new FakeDecisionMapper();
    private final DecisionService decisionService = new DecisionServiceImpl(decisionMapper);

    @Test
    void createDecisionStoresCurrentUserAndInitialStatus() {
        DecisionCreateRequest request = new DecisionCreateRequest(
                "是否报名周末课程",
                "想提升技能，但担心时间不够",
                "报名,再观望",
                "先小成本尝试",
                "学习",
                "平静",
                2,
                LocalDateTime.of(2026, 6, 1, 10, 0)
        );

        Decision created = decisionService.create(7L, request);

        assertEquals(7L, created.userId());
        assertEquals("是否报名周末课程", created.title());
        assertEquals("pending", created.status());
        assertNotNull(created.id());
    }

    @Test
    void createDecisionAllowsBlankContextAndStoresEmptyString() {
        DecisionCreateRequest request = new DecisionCreateRequest(
                "是否调整学习计划",
                null,
                "{\"version\":1,\"selectedId\":\"opt_1\",\"items\":[{\"id\":\"opt_1\",\"title\":\"早起学习\",\"children\":[]},{\"id\":\"opt_2\",\"title\":\"晚上学习\",\"children\":[]}]}",
                "想减少拖延",
                "学习",
                "平静",
                2,
                LocalDateTime.of(2026, 6, 3, 10, 0)
        );

        Decision created = decisionService.create(7L, request);

        assertEquals("", created.context());
        assertEquals("是否调整学习计划", created.title());
    }

    @Test
    void dashboardSummaryCountsOnlyCurrentUser() {
        decisionMapper.seed(new Decision(1L, 7L, "A", "ctx", "a,b", "reason", "学习", "平静", 2,
                LocalDateTime.now().minusDays(1), "满意", "不错", "reviewed", LocalDateTime.now(), LocalDateTime.now()));
        decisionMapper.seed(new Decision(2L, 7L, "B", "ctx", "a,b", "reason", "消费", "犹豫", 1,
                LocalDateTime.now().plusDays(1), null, null, "pending", LocalDateTime.now(), LocalDateTime.now()));
        decisionMapper.seed(new Decision(3L, 8L, "C", "ctx", "a,b", "reason", "工作", "焦虑", 3,
                LocalDateTime.now().plusDays(1), null, null, "pending", LocalDateTime.now(), LocalDateTime.now()));

        var summary = decisionService.summary(7L);

        assertEquals(2, summary.total());
        assertEquals(1, summary.pending());
        assertEquals(1, summary.reviewed());
        assertEquals(1, summary.satisfaction().get("满意"));
    }

    @Test
    void satisfactionPieFiltersReviewedResultsByTagAndMood() {
        AnalysisServiceImpl analysisService = new AnalysisServiceImpl(decisionMapper);
        LocalDateTime now = LocalDateTime.now();
        decisionMapper.seed(new Decision(1L, 7L, "A", "ctx", "a,b", "reason", "学习,工作", "平静", 2,
                now.minusDays(1), "满意", "不错", "reviewed", now, now));
        decisionMapper.seed(new Decision(2L, 7L, "B", "ctx", "a,b", "reason", "学习", "平静", 2,
                now.minusDays(1), "一般", "还行", "reviewed", now, now));
        decisionMapper.seed(new Decision(3L, 7L, "C", "ctx", "a,b", "reason", "学习", "焦虑", 2,
                now.minusDays(1), "后悔", "不合适", "reviewed", now, now));
        decisionMapper.seed(new Decision(4L, 7L, "D", "ctx", "a,b", "reason", "消费", "平静", 2,
                now.minusDays(1), "满意", "不错", "reviewed", now, now));
        decisionMapper.seed(new Decision(5L, 8L, "E", "ctx", "a,b", "reason", "学习", "平静", 2,
                now.minusDays(1), "满意", "不错", "reviewed", now, now));
        decisionMapper.seed(new Decision(6L, 7L, "F", "ctx", "a,b", "reason", "学习", "平静", 2,
                now.plusDays(1), null, null, "pending", now, now));

        var pie = analysisService.satisfactionPie(7L, " 学习 ", " 平静 ");

        assertEquals(2, pie.total());
        assertEquals("满意", pie.items().get(0).name());
        assertEquals(1, pie.items().get(0).value());
        assertEquals("一般", pie.items().get(1).name());
        assertEquals(1, pie.items().get(1).value());
        assertEquals("后悔", pie.items().get(2).name());
        assertEquals(0, pie.items().get(2).value());
    }

    @Test
    void trendLineCountsCurrentUserDecisionsByCreateMonth() {
        AnalysisServiceImpl analysisService = new AnalysisServiceImpl(decisionMapper);
        decisionMapper.seed(new Decision(1L, 7L, "A", "ctx", "a,b", "reason", "学习", "平静", 2,
                LocalDateTime.of(2026, 1, 8, 10, 0), null, null, "pending",
                LocalDateTime.of(2026, 1, 3, 10, 0), LocalDateTime.of(2026, 1, 3, 10, 0)));
        decisionMapper.seed(new Decision(2L, 7L, "B", "ctx", "a,b", "reason", "工作", "纠结", 2,
                LocalDateTime.of(2026, 1, 9, 10, 0), null, null, "pending",
                LocalDateTime.of(2026, 1, 20, 10, 0), LocalDateTime.of(2026, 1, 20, 10, 0)));
        decisionMapper.seed(new Decision(3L, 7L, "C", "ctx", "a,b", "reason", "生活", "平静", 2,
                LocalDateTime.of(2026, 2, 2, 10, 0), "满意", "不错", "reviewed",
                LocalDateTime.of(2026, 2, 1, 10, 0), LocalDateTime.of(2026, 2, 1, 10, 0)));
        decisionMapper.seed(new Decision(4L, 8L, "D", "ctx", "a,b", "reason", "学习", "平静", 2,
                LocalDateTime.of(2026, 1, 8, 10, 0), null, null, "pending",
                LocalDateTime.of(2026, 1, 5, 10, 0), LocalDateTime.of(2026, 1, 5, 10, 0)));
        decisionMapper.seed(new Decision(5L, 7L, "E", "ctx", "a,b", "reason", "消费", "冲动", 2,
                LocalDateTime.of(2026, 3, 8, 10, 0), null, null, "pending",
                LocalDateTime.of(2026, 3, 1, 10, 0), LocalDateTime.of(2026, 3, 1, 10, 0)));
        decisionMapper.deletedDecisionIds.add(5L);

        var trend = analysisService.trendLine(7L, null);

        assertEquals(3, trend.total());
        assertEquals("month", trend.granularity());
        assertEquals("2026-01", trend.labels().get(0));
        assertEquals("2026-12", trend.labels().get(11));
        assertEquals(12, trend.labels().size());
        assertEquals(2, trend.counts().get(0));
        assertEquals(1, trend.counts().get(1));
        assertEquals(0, trend.counts().get(2));
    }

    @Test
    void trendLineCountsCurrentUserDecisionsByDayWhenMonthSelected() {
        AnalysisServiceImpl analysisService = new AnalysisServiceImpl(decisionMapper);
        decisionMapper.seed(new Decision(1L, 7L, "A", "ctx", "a,b", "reason", "学习", "平静", 2,
                LocalDateTime.of(2026, 1, 8, 10, 0), null, null, "pending",
                LocalDateTime.of(2026, 1, 3, 10, 0), LocalDateTime.of(2026, 1, 3, 10, 0)));
        decisionMapper.seed(new Decision(2L, 7L, "B", "ctx", "a,b", "reason", "工作", "纠结", 2,
                LocalDateTime.of(2026, 1, 9, 10, 0), null, null, "pending",
                LocalDateTime.of(2026, 1, 3, 15, 0), LocalDateTime.of(2026, 1, 3, 15, 0)));
        decisionMapper.seed(new Decision(3L, 7L, "C", "ctx", "a,b", "reason", "生活", "平静", 2,
                LocalDateTime.of(2026, 1, 16, 10, 0), "满意", "不错", "reviewed",
                LocalDateTime.of(2026, 1, 15, 10, 0), LocalDateTime.of(2026, 1, 15, 10, 0)));
        decisionMapper.seed(new Decision(4L, 8L, "D", "ctx", "a,b", "reason", "学习", "平静", 2,
                LocalDateTime.of(2026, 1, 8, 10, 0), null, null, "pending",
                LocalDateTime.of(2026, 1, 3, 10, 0), LocalDateTime.of(2026, 1, 3, 10, 0)));
        decisionMapper.seed(new Decision(5L, 7L, "E", "ctx", "a,b", "reason", "消费", "冲动", 2,
                LocalDateTime.of(2026, 2, 1, 10, 0), null, null, "pending",
                LocalDateTime.of(2026, 2, 1, 10, 0), LocalDateTime.of(2026, 2, 1, 10, 0)));

        var trend = analysisService.trendLine(7L, "2026-01");

        assertEquals(3, trend.total());
        assertEquals("day", trend.granularity());
        assertEquals("2026-01", trend.selectedMonth());
        assertEquals(31, trend.labels().size());
        assertEquals("01日", trend.labels().get(0));
        assertEquals("31日", trend.labels().get(30));
        assertEquals(2, trend.counts().get(2));
        assertEquals(1, trend.counts().get(14));
    }

    @Test
    void dashboardUsesDatabaseSummaryAndOnlyDuePendingReview() {
        LocalDateTime now = LocalDateTime.now();
        decisionMapper.summary = new com.exam.exam_backed.decision.vo.DecisionSummary(
                35,
                12,
                23,
                java.util.Map.of("满意", 8, "一般", 4, "后悔", 2)
        );
        for (int i = 1; i <= 25; i++) {
            decisionMapper.seed(new Decision((long) i, 7L, "recent-" + i, "ctx", "a,b", "reason", "学习", "平静", 2,
                    now.plusDays(i), null, null, "pending", now.minusMinutes(i), now.minusMinutes(i)));
        }
        Decision duePending = new Decision(100L, 7L, "到期回看", "ctx", "a,b", "reason", "生活", "犹豫", 2,
                now.minusHours(1), null, null, "pending", now.minusDays(1), now.minusDays(1));
        Decision futurePending = new Decision(101L, 7L, "未来回看", "ctx", "a,b", "reason", "消费", "平静", 1,
                now.plusDays(3), null, null, "pending", now.minusDays(1), now.minusDays(1));
        Decision reviewed = new Decision(102L, 7L, "已回测", "ctx", "a,b", "reason", "学习", "平静", 2,
                now.minusDays(2), "满意", "不错", "reviewed", now.minusDays(1), now.minusDays(1));
        decisionMapper.duePendingReview = List.of(duePending);
        decisionMapper.seed(duePending);
        decisionMapper.seed(futurePending);
        decisionMapper.seed(reviewed);

        var dashboard = decisionService.dashboard(7L);

        assertEquals(35, dashboard.summary().total());
        assertEquals(12, dashboard.summary().pending());
        assertEquals(23, dashboard.summary().reviewed());
        assertEquals(8, dashboard.summary().satisfaction().get("满意"));
        assertEquals(20, dashboard.recent().size());
        assertEquals(List.of(duePending), dashboard.pendingReview());
    }

    @Test
    void searchFiltersByKeywordTagStatusAndCurrentUser() {
        LocalDateTime now = LocalDateTime.now();
        Decision matched = new Decision(1L, 7L, "Weekend Course", "ctx", "a,b", "reason", "study,life", "calm", 2,
                now.plusDays(1), null, null, "pending", now.minusMinutes(1), now.minusMinutes(1));
        decisionMapper.seed(matched);
        decisionMapper.seed(new Decision(2L, 7L, "Weekend Course Reviewed", "ctx", "a,b", "reason", "study", "calm", 2,
                now.minusDays(1), "ok", "done", "reviewed", now.minusMinutes(2), now.minusMinutes(2)));
        decisionMapper.seed(new Decision(3L, 7L, "Other Plan", "ctx", "a,b", "reason", "study", "calm", 2,
                now.plusDays(1), null, null, "pending", now.minusMinutes(3), now.minusMinutes(3)));
        decisionMapper.seed(new Decision(4L, 8L, "Weekend Course", "ctx", "a,b", "reason", "study", "calm", 2,
                now.plusDays(1), null, null, "pending", now.minusMinutes(4), now.minusMinutes(4)));

        List<Decision> results = decisionService.search(7L, " Course ", " study ", "pending", 50);

        assertEquals(List.of(matched), results);
    }

    @Test
    void searchRejectsInvalidStatus() {
        BusinessException error = assertThrows(BusinessException.class, () ->
                decisionService.search(7L, null, null, "deleted", 50)
        );

        assertEquals("决策状态筛选值不正确", error.getMessage());
    }

    @Test
    void createDecisionReturnsInsertedRecordByGeneratedId() {
        DecisionCreateRequest request = new DecisionCreateRequest(
                "是否买运动手环",
                "想记录睡眠但不确定是否必要",
                "购买,暂缓",
                "先用便宜设备试试",
                "消费",
                "犹豫",
                1,
                LocalDateTime.of(2026, 6, 2, 10, 0)
        );

        Decision created = decisionService.create(7L, request);

        assertEquals(1L, created.id());
        assertEquals("是否买运动手环", created.title());
        assertEquals(7L, created.userId());
    }

    @Test
    void reviewRejectsDecisionFromAnotherUser() {
        decisionMapper.seed(new Decision(1L, 8L, "A", "ctx", "a,b", "reason", "学习", "平静", 2,
                LocalDateTime.now(), null, null, "pending", LocalDateTime.now(), LocalDateTime.now()));

        BusinessException error = assertThrows(BusinessException.class, () -> decisionService.review(
                7L,
                1L,
                new DecisionReviewRequest("满意", "结果比预期更好")
        ));

        assertEquals("决策记录不存在", error.getMessage());
    }

    @Test
    void reviewUpdatesSatisfactionFeedbackAndStatus() {
        decisionMapper.seed(new Decision(1L, 7L, "A", "ctx", "a,b", "reason", "学习", "平静", 2,
                LocalDateTime.now(), null, null, "pending", LocalDateTime.now(), LocalDateTime.now()));

        Decision reviewed = decisionService.review(7L, 1L, new DecisionReviewRequest("一般", "需要继续观察"));

        assertEquals("一般", reviewed.satisfaction());
        assertEquals("需要继续观察", reviewed.feedback());
        assertEquals("reviewed", reviewed.status());
    }

    @Test
    void reviewReturnsLatestRecordAfterDatabaseUpdate() {
        LocalDateTime now = LocalDateTime.now();
        decisionMapper.seed(new Decision(1L, 7L, "A", "ctx", "a,b", "reason", "学习", "平静", 2,
                now, null, null, "pending", now, now));
        decisionMapper.recordAfterReview = new Decision(1L, 7L, "A", "ctx", "a,b", "reason", "学习", "平静", 2,
                now, "后悔", "结果不如预期", "reviewed", now, now.plusMinutes(1));

        Decision reviewed = decisionService.review(7L, 1L, new DecisionReviewRequest("后悔", "结果不如预期"));

        assertSame(decisionMapper.recordAfterReview, reviewed);
    }

    @Test
    void detailReturnsCurrentUserDecisionAndSelectedOption() {
        LocalDateTime now = LocalDateTime.of(2026, 6, 1, 10, 0);
        decisionMapper.seed(new Decision(
                1L,
                7L,
                "是否调整学习计划",
                "晚上效率不稳定",
                "{\"version\":1,\"selectedId\":\"opt_2\",\"items\":[{\"id\":\"opt_1\",\"title\":\"早起学习\",\"children\":[]},{\"id\":\"opt_2\",\"title\":\"晚间学习\",\"children\":[{\"id\":\"child_1\",\"title\":\"固定 2 小时\"}]}]}",
                "先顺着当前作息调整",
                "学习",
                "平静",
                2,
                now.plusDays(7),
                null,
                null,
                "pending",
                now,
                now
        ));

        var detail = decisionService.detail(7L, 1L);

        assertEquals(1L, detail.id());
        assertEquals("是否调整学习计划", detail.title());
        assertEquals("晚上效率不稳定", detail.context());
        assertEquals("晚间学习", detail.finalChoice());
        assertEquals("先顺着当前作息调整", detail.reason());
        assertEquals("pending", detail.status());
        assertEquals(now, detail.createTime());
        assertEquals(2, detail.options().size());
        assertEquals("固定 2 小时", detail.options().get(1).children().get(0).title());
    }

    @Test
    void detailRejectsDecisionFromAnotherUser() {
        LocalDateTime now = LocalDateTime.now();
        decisionMapper.seed(new Decision(1L, 8L, "A", "ctx", "a,b", "reason", "学习", "平静", 2,
                now, null, null, "pending", now, now));

        BusinessException error = assertThrows(BusinessException.class, () -> decisionService.detail(7L, 1L));

        assertEquals("决策记录不存在", error.getMessage());
    }

    @Test
    void detailSupportsLegacyCommaSeparatedOptions() {
        LocalDateTime now = LocalDateTime.now();
        decisionMapper.seed(new Decision(1L, 7L, "A", "ctx", "报名,自学", "reason", "学习", "平静", 2,
                now, null, null, "pending", now, now));

        var detail = decisionService.detail(7L, 1L);

        assertEquals("报名", detail.finalChoice());
        assertEquals(2, detail.options().size());
        assertEquals("报名", detail.options().get(0).title());
        assertEquals("自学", detail.options().get(1).title());
    }

    @Test
    void softDeleteOnlyRemovesCurrentUsersDecisionFromReadsAndSummary() {
        LocalDateTime now = LocalDateTime.now();
        decisionMapper.seed(new Decision(1L, 7L, "A", "ctx", "a,b", "reason", "学习", "平静", 2,
                now.minusHours(1), null, null, "pending", now.minusDays(1), now.minusDays(1)));
        decisionMapper.seed(new Decision(2L, 8L, "B", "ctx", "a,b", "reason", "学习", "平静", 2,
                now.minusHours(1), null, null, "pending", now.minusDays(1), now.minusDays(1)));

        decisionService.delete(7L, 1L);

        assertThrows(BusinessException.class, () -> decisionService.detail(7L, 1L));
        assertEquals(0, decisionService.summary(7L).total());
        assertTrue(decisionService.recent(7L, 20).isEmpty());
        assertTrue(decisionService.search(7L, null, null, null, 50).isEmpty());
        assertTrue(decisionService.dashboard(7L).pendingReview().isEmpty());
        assertEquals(1, decisionService.summary(8L).total());
    }

    @Test
    void deleteRejectsMissingOrDeletedDecision() {
        BusinessException missing = assertThrows(BusinessException.class, () -> decisionService.delete(7L, 99L));
        assertEquals("决策记录不存在", missing.getMessage());

        LocalDateTime now = LocalDateTime.now();
        decisionMapper.seed(new Decision(1L, 7L, "A", "ctx", "a,b", "reason", "学习", "平静", 2,
                now, null, null, "pending", now, now));
        decisionService.delete(7L, 1L);

        BusinessException deleted = assertThrows(BusinessException.class, () -> decisionService.delete(7L, 1L));
        assertEquals("决策记录不存在", deleted.getMessage());
    }

    private static class FakeDecisionMapper implements DecisionMapper {
        private final AtomicLong idGenerator = new AtomicLong(1);
        private final List<Decision> decisions = new ArrayList<>();
        private final List<Long> deletedDecisionIds = new ArrayList<>();
        private com.exam.exam_backed.decision.vo.DecisionSummary summary;
        private List<Decision> duePendingReview = new ArrayList<>();
        private Decision recordAfterReview;

        void seed(Decision decision) {
            decisions.add(decision);
        }

        @Override
        public int insert(Decision decision) {
            decisions.add(decision.withId(idGenerator.getAndIncrement()));
            return 1;
        }

        @Override
        public Long lastInsertedId() {
            return idGenerator.get() - 1;
        }

        @Override
        public Optional<Decision> findByIdAndUserId(Long id, Long userId) {
            return decisions.stream()
                    .filter(decision -> decision.id().equals(id) && decision.userId().equals(userId))
                    .filter(decision -> !deletedDecisionIds.contains(decision.id()))
                    .findFirst();
        }

        @Override
        public List<Decision> findRecentByUserId(Long userId, int limit) {
            return decisions.stream()
                    .filter(decision -> decision.userId().equals(userId))
                    .filter(decision -> !deletedDecisionIds.contains(decision.id()))
                    .limit(limit)
                    .toList();
        }

        @Override
        public List<Decision> searchByUserId(Long userId, String keyword, String tag, String status, int limit) {
            return decisions.stream()
                    .filter(decision -> decision.userId().equals(userId))
                    .filter(decision -> !deletedDecisionIds.contains(decision.id()))
                    .filter(decision -> keyword == null || decision.title().contains(keyword))
                    .filter(decision -> tag == null || (decision.tags() != null && decision.tags().contains(tag)))
                    .filter(decision -> status == null || status.equals(decision.status()))
                    .limit(limit)
                    .toList();
        }

        @Override
        public List<Decision> findDuePendingReviewByUserId(Long userId, int limit) {
            return duePendingReview.stream()
                    .filter(decision -> decision.userId().equals(userId))
                    .filter(decision -> !deletedDecisionIds.contains(decision.id()))
                    .limit(limit)
                    .toList();
        }

        @Override
        public int countByUserId(Long userId) {
            if (summary != null) {
                return summary.total();
            }
            return (int) decisions.stream()
                    .filter(decision -> decision.userId().equals(userId))
                    .filter(decision -> !deletedDecisionIds.contains(decision.id()))
                    .count();
        }

        @Override
        public int countByUserIdAndStatus(Long userId, String status) {
            if (summary != null && "pending".equals(status)) {
                return summary.pending();
            }
            if (summary != null && "reviewed".equals(status)) {
                return summary.reviewed();
            }
            return (int) decisions.stream()
                    .filter(decision -> decision.userId().equals(userId))
                    .filter(decision -> !deletedDecisionIds.contains(decision.id()))
                    .filter(decision -> status.equals(decision.status()))
                    .count();
        }

        @Override
        public int countReviewedBySatisfaction(Long userId, String satisfactionLabel) {
            if (summary != null) {
                return summary.satisfaction().getOrDefault(satisfactionLabel, 0);
            }
            int total = 0;
            for (Decision decision : decisions) {
                if (!decision.userId().equals(userId)) {
                    continue;
                }
                if (deletedDecisionIds.contains(decision.id())) {
                    continue;
                }
                if ("reviewed".equals(decision.status()) && satisfactionLabel.equals(decision.satisfaction())) {
                    total++;
                }
            }
            return total;
        }

        @Override
        public int countReviewedBySatisfactionAndFilters(Long userId, String satisfactionLabel, String tag, String mood) {
            int total = 0;
            for (Decision decision : decisions) {
                if (!decision.userId().equals(userId)) {
                    continue;
                }
                if (deletedDecisionIds.contains(decision.id())) {
                    continue;
                }
                if (!"reviewed".equals(decision.status()) || !satisfactionLabel.equals(decision.satisfaction())) {
                    continue;
                }
                if (tag != null && (decision.tags() == null || !decision.tags().contains(tag))) {
                    continue;
                }
                if (mood != null && !mood.equals(decision.mood())) {
                    continue;
                }
                total++;
            }
            return total;
        }

        @Override
        public List<TrendCount> countCreatedByMonth(Long userId, LocalDateTime start, LocalDateTime end) {
            return decisions.stream()
                    .filter(decision -> decision.userId().equals(userId))
                    .filter(decision -> !deletedDecisionIds.contains(decision.id()))
                    .filter(decision -> !decision.createTime().isBefore(start) && decision.createTime().isBefore(end))
                    .collect(Collectors.groupingBy(
                            decision -> "%04d-%02d".formatted(decision.createTime().getYear(), decision.createTime().getMonthValue()),
                            Collectors.summingInt(decision -> 1)
                    ))
                    .entrySet()
                    .stream()
                    .sorted(Comparator.comparing(entry -> entry.getKey()))
                    .map(entry -> new TrendCount(entry.getKey(), entry.getValue()))
                    .toList();
        }

        @Override
        public List<TrendCount> countCreatedByDay(Long userId, LocalDateTime start, LocalDateTime end) {
            return decisions.stream()
                    .filter(decision -> decision.userId().equals(userId))
                    .filter(decision -> !deletedDecisionIds.contains(decision.id()))
                    .filter(decision -> !decision.createTime().isBefore(start) && decision.createTime().isBefore(end))
                    .collect(Collectors.groupingBy(
                            decision -> "%02d".formatted(decision.createTime().getDayOfMonth()),
                            Collectors.summingInt(decision -> 1)
                    ))
                    .entrySet()
                    .stream()
                    .sorted(Comparator.comparing(entry -> entry.getKey()))
                    .map(entry -> new TrendCount(entry.getKey(), entry.getValue()))
                    .toList();
        }

        @Override
        public int updateReview(Long id, Long userId, String satisfaction, String feedback, String status) {
            for (int i = 0; i < decisions.size(); i++) {
                Decision decision = decisions.get(i);
                if (decision.id().equals(id) && decision.userId().equals(userId)) {
                    decisions.set(i, recordAfterReview == null ? decision.withReview(satisfaction, feedback, status) : recordAfterReview);
                    return 1;
                }
            }
            return 0;
        }

        @Override
        public int softDelete(Long id, Long userId) {
            Optional<Decision> decision = findByIdAndUserId(id, userId);
            if (decision.isEmpty()) {
                return 0;
            }
            deletedDecisionIds.add(id);
            return 1;
        }
    }
}
