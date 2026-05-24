package com.exam.exam_backed.advice;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.exam.exam_backed.advice.dto.AdviceGenerateRequest;
import com.exam.exam_backed.advice.mapper.SystemConfigMapper;
import com.exam.exam_backed.advice.service.impl.AdviceServiceImpl;
import com.exam.exam_backed.decision.Decision;
import com.exam.exam_backed.decision.mapper.DecisionMapper;
import com.exam.exam_backed.support.AbstractBaseMapperStub;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdviceServiceImplTest {
    private final FakeSystemConfigMapper systemConfigMapper = new FakeSystemConfigMapper();
    private final FakeDecisionMapper decisionMapper = new FakeDecisionMapper();
    private HttpServer server;
    private String capturedRequestBody = "";

    @AfterEach
    void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void generateIncludesCurrentUsersRelatedReviewedHistoryAndExcludesCurrentDecision() throws IOException {
        startAiServer();
        systemConfigMapper.baseUrl = "http://127.0.0.1:" + server.getAddress().getPort() + "/v1";
        LocalDateTime now = LocalDateTime.now();
        decisionMapper.seed(new Decision(99L, 7L, "是否开始晚间跑步", "current", "run,walk", "reason", "运动,健康", "中", 2,
                now, null, null, "pending", now.minusDays(1), now.minusDays(1)));
        decisionMapper.seed(new Decision(1L, 7L, "晚间跑步20分钟", "久坐后恢复运动", "run,walk", "low cost", "运动,健康", "中", 2,
                now.minusDays(10), "满意", "执行门槛低，睡眠更好", "reviewed", now.minusDays(12), now.minusDays(9)));
        decisionMapper.seed(new Decision(2L, 7L, "午休后快走", "恢复运动习惯", "walk", "easy", "健康", "低", 1,
                now.minusDays(20), "一般", "天气影响执行", "reviewed", now.minusDays(22), now.minusDays(19)));
        decisionMapper.seed(new Decision(3L, 8L, "晚间跑步20分钟 其他用户", "other user", "run", "other", "运动", "中", 2,
                now.minusDays(5), "后悔", "其他用户数据", "reviewed", now.minusDays(6), now.minusDays(4)));

        AdviceGenerateRequest request = new AdviceGenerateRequest(
                99L,
                "review",
                "是否开始晚间跑步",
                "最近久坐偏多，希望用低门槛方式恢复运动。",
                "晚间跑步20分钟,下班散步",
                "晚间跑步20分钟",
                "希望恢复运动",
                "运动,健康",
                "中",
                2,
                now,
                "满意",
                "执行后感觉不错",
                ""
        );

        new AdviceServiceImpl(systemConfigMapper, decisionMapper).generate(request, 7L);

        assertTrue(capturedRequestBody.contains("历史决策数据"));
        assertTrue(capturedRequestBody.contains("同标签历史回测"));
        assertTrue(capturedRequestBody.contains("晚间跑步20分钟"));
        assertTrue(capturedRequestBody.contains("午休后快走"));
        assertTrue(capturedRequestBody.contains("满意 1"));
        assertTrue(capturedRequestBody.contains("一般 1"));
        assertFalse(capturedRequestBody.contains("当前决策重复计入"));
        assertFalse(capturedRequestBody.contains("其他用户数据"));
    }

    private void startAiServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/v1/chat/completions", exchange -> {
            capturedRequestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            String body = """
                    {"choices":[{"message":{"content":"{\\"summary\\":\\"ok\\",\\"factors\\":\\"ok\\",\\"risks\\":\\"ok\\",\\"improvements\\":[\\"ok\\"],\\"nextReminder\\":\\"ok\\"}"}}]}
                    """;
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });
        server.start();
    }

    private static class FakeSystemConfigMapper extends AbstractBaseMapperStub<SystemConfig> implements SystemConfigMapper {
        private String baseUrl;

        @Override
        public Optional<String> findValueByKey(String configKey) {
            return switch (configKey) {
                case "advice.ai.api_key" -> Optional.of("test-key");
                case "advice.ai.base_url" -> Optional.of(baseUrl);
                case "advice.ai.model" -> Optional.of("test-model");
                default -> Optional.empty();
            };
        }
    }

    private static class FakeDecisionMapper extends AbstractBaseMapperStub<Decision> implements DecisionMapper {
        private final List<Decision> decisions = new ArrayList<>();

        void seed(Decision decision) {
            decisions.add(decision);
        }

        @Override
        public List<Decision> selectList(Wrapper<Decision> queryWrapper) {
            return decisions;
        }

        @Override
        public Optional<Decision> findByIdAndUserId(Long id, Long userId) {
            return decisions.stream()
                    .filter(decision -> id.equals(decision.id()))
                    .filter(decision -> userId.equals(decision.userId()))
                    .findFirst();
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
}
