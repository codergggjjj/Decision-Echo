package com.exam.exam_backed.advice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.exam.exam_backed.advice.dto.AdviceGenerateRequest;
import com.exam.exam_backed.advice.mapper.SystemConfigMapper;
import com.exam.exam_backed.advice.service.AdviceService;
import com.exam.exam_backed.advice.vo.DecisionAdviceResponse;
import com.exam.exam_backed.common.BusinessException;
import com.exam.exam_backed.common.ErrorCode;
import com.exam.exam_backed.decision.Decision;
import com.exam.exam_backed.decision.DecisionGoal;
import com.exam.exam_backed.decision.mapper.DecisionGoalMapper;
import com.exam.exam_backed.decision.mapper.DecisionMapper;
import com.exam.exam_backed.goal.Goal;
import com.exam.exam_backed.goal.mapper.GoalMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdviceServiceImpl implements AdviceService {
    private static final String API_KEY_CONFIG = "advice.ai.api_key";
    private static final String BASE_URL_CONFIG = "advice.ai.base_url";
    private static final String MODEL_CONFIG = "advice.ai.model";
    private static final String DEFAULT_BASE_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1";
    private static final String DEFAULT_MODEL = "qwen-plus";

    private final SystemConfigMapper systemConfigMapper;
    private final DecisionMapper decisionMapper;
    private final DecisionGoalMapper decisionGoalMapper;
    private final GoalMapper goalMapper;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public AdviceServiceImpl(SystemConfigMapper systemConfigMapper, DecisionMapper decisionMapper) {
        this(systemConfigMapper, decisionMapper, null, null);
    }

    public AdviceServiceImpl(SystemConfigMapper systemConfigMapper, DecisionMapper decisionMapper, GoalMapper goalMapper) {
        this(systemConfigMapper, decisionMapper, null, goalMapper);
    }

    @Autowired
    public AdviceServiceImpl(SystemConfigMapper systemConfigMapper, DecisionMapper decisionMapper, DecisionGoalMapper decisionGoalMapper, GoalMapper goalMapper) {
        this.systemConfigMapper = systemConfigMapper;
        this.decisionMapper = decisionMapper;
        this.decisionGoalMapper = decisionGoalMapper;
        this.goalMapper = goalMapper;
        this.objectMapper = new ObjectMapper();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(8))
                .build();
    }

    @Override
    public DecisionAdviceResponse generate(AdviceGenerateRequest request, Long userId) {
        AiSettings settings = loadAiSettings();
        if (settings.apiKey().isBlank()) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI 服务未配置，请先设置 API Key");
        }
        validateCurrentDecisionAccess(request, userId);
        return requestAdvice(request, settings, buildHistorySummary(request, userId), userId);
    }

    private void validateCurrentDecisionAccess(AdviceGenerateRequest request, Long userId) {
        if (request.decisionId() == null) {
            return;
        }
        if (userId == null || decisionMapper.findByIdAndUserId(request.decisionId(), userId).isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "决策记录不存在");
        }
    }

    private AiSettings loadAiSettings() {
        return new AiSettings(
                configValue(API_KEY_CONFIG, ""),
                configValue(BASE_URL_CONFIG, DEFAULT_BASE_URL),
                configValue(MODEL_CONFIG, DEFAULT_MODEL)
        );
    }

    private String configValue(String key, String defaultValue) {
        try {
            return systemConfigMapper.findValueByKey(key)
                    .map(String::trim)
                    .filter(value -> !value.isBlank())
                    .orElse(defaultValue);
        } catch (DataAccessException exception) {
            return defaultValue;
        }
    }

    private DecisionAdviceResponse requestAdvice(AdviceGenerateRequest decision, AiSettings settings, String historySummary, Long userId) {
        try {
            String requestBody = buildRequestBody(decision, settings.model(), historySummary, userId);
            HttpRequest request = HttpRequest.newBuilder(chatCompletionsUri(settings.baseUrl()))
                    .timeout(Duration.ofSeconds(30))
                    .header("Authorization", "Bearer " + settings.apiKey())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI 建议生成失败，请稍后重试");
            }
            String content = extractFirstContent(response.body());
            if (content.isBlank()) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI 建议生成失败，请稍后重试");
            }
            return normalizeAdvice(objectMapper.readValue(cleanJsonContent(content), DecisionAdviceResponse.class));
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI 建议生成失败，请稍后重试");
        }
    }

    private URI chatCompletionsUri(String baseUrl) {
        String normalizedBaseUrl = baseUrl == null ? "" : baseUrl.trim();
        if (normalizedBaseUrl.endsWith("/")) {
            normalizedBaseUrl = normalizedBaseUrl.substring(0, normalizedBaseUrl.length() - 1);
        }
        return URI.create(normalizedBaseUrl + "/chat/completions");
    }

    private String buildRequestBody(AdviceGenerateRequest decision, String model, String historySummary, Long userId) {
        boolean reviewMode = isReviewMode(decision);
        String goalContext = buildGoalContext(decision, userId);
        return """
                {"model":"%s","temperature":0.3,"response_format":{"type":"json_object"},"messages":[{"role":"system","content":"%s"},{"role":"user","content":"%s"}]}
                """.formatted(
                escapeJson(model),
                escapeJson(reviewMode
                        ? "你是一个理性、温和、客观的个人决策复盘助手。你必须只输出合法 JSON，不要输出 Markdown，不要输出 JSON 以外的解释。"
                        : "你是一个理性、温和、客观的个人决策分析助手。你必须只输出合法 JSON，不要输出 Markdown，不要输出 JSON 以外的解释。"),
                escapeJson(reviewMode ? buildReviewPrompt(decision, historySummary, goalContext) : buildCreatePrompt(decision, historySummary, goalContext))
        ).trim();
    }

    private boolean isReviewMode(AdviceGenerateRequest decision) {
        return "review".equalsIgnoreCase(fallback(decision.mode(), ""));
    }

    private String buildCreatePrompt(AdviceGenerateRequest decision, String historySummary, String goalContext) {
        return """
                请根据用户提供的决策背景、候选方案和历史决策数据，对每个候选方案进行利弊分析。

                重要要求：
                1. 不要替用户直接做最终决定。
                2. 不要使用绝对化语气，例如“必须选”“一定不要选”。
                3. 不要编造用户没有提供的信息。
                4. 历史数据只作为参考；如果历史数据较少，必须明确说明建议主要基于当前决策信息。
                5. 分析要简洁、具体、可执行。
                6. 如果提供了长期目标信息，必须额外分析每个候选方案对长期目标的契合度；如果关联了多个目标，请给出整体契合度和逐目标简要分析。
                7. 输出必须是合法 JSON，不要输出 Markdown，不要输出 JSON 以外的解释。

                用户决策信息：
                决策标题：%s
                决策背景：%s
                决策标签：%s
                当前心情：%s
                紧急程度：%s
                历史决策数据：
                %s
                长期目标信息：
                %s

                候选方案：
                %s

                请严格返回以下 JSON 结构：
                {
                  "overallAdvice": "整体分析总结，1-2句话",
                  "options": [
                    {
                      "name": "候选方案名称",
                      "pros": ["优点1", "优点2"],
                      "cons": ["缺点1", "缺点2"],
                      "risks": ["风险1"],
                      "bestFor": "这个方案适合什么情况",
                      "suggestion": "对这个方案的补充建议"
                    }
                  ],
                  "reminder": "最终选择前需要注意的一句话",
                  "goalAlignment": {
                    "score": 0,
                    "level": "无长期目标/低度契合/中度契合/高度契合",
                    "bestOption": "最契合长期目标的候选方案名称",
                    "reason": "为什么该方案更契合长期目标",
                    "optionAnalysis": [
                      { "option": "候选方案名称", "score": 0, "comment": "该方案与长期目标的关系" }
                    ]
                  }
                }
                """.formatted(
                fallback(decision.title(), "未填写标题"),
                fallback(decision.context(), "未填写背景"),
                fallback(decision.tags(), "未填写标签"),
                fallback(decision.mood(), "未填写心情"),
                decision.urgency(),
                historySummary,
                fallback(goalContext, "未关联长期目标"),
                fallback(decision.options(), "未填写候选方案")
        );
    }

    private String buildReviewPrompt(AdviceGenerateRequest decision, String historySummary, String goalContext) {
        return """
                请根据用户提供的当前决策记录、回测结果和数据库中的相关历史决策数据，生成结构清晰、语气温和、可执行的复盘建议。

                重要要求：
                1. 不要替用户做绝对判断。
                2. 不要使用“你必须”“你一定错了”“绝对应当”等强制或责备语气。
                3. 不要编造用户没有提供的信息。
                4. 历史数据只作为参考；如果历史数据较少，必须明确说明建议主要基于当前决策信息。
                5. 建议要具体、简洁、可执行。
                6. 如果提供了长期目标信息，必须额外分析这次选择与长期目标的契合度；如果关联了多个目标，请给出整体契合度和逐目标简要分析。
                7. 总字数控制在 300 字以内。
                8. 必须严格按照指定 JSON 格式输出，不要输出 Markdown 或 JSON 以外的解释。

                用户当前决策信息：
                决策标题：%s
                决策背景：%s
                候选方案：%s
                最终选择：%s
                选择原因：%s
                决策标签：%s
                决策时心情：%s
                紧急程度：%s
                回测满意度：%s
                回测反馈：%s
                历史决策数据：
                %s
                长期目标信息：
                %s

                请严格返回以下 JSON 格式：
                {
                  "summary": "决策概括：用 1-2 句话概括这次决策的核心内容和结果。",
                  "factors": "影响因素：分析当时影响用户选择的主要因素。",
                  "risks": "风险问题：指出这次决策中可能存在的不足或可反思的地方，语气温和。",
                  "improvements": [
                    "改进建议1：一条具体、可执行的建议。",
                    "改进建议2：一条具体、可执行的建议。",
                    "改进建议3：一条具体、可执行的建议。"
                  ],
                  "nextReminder": "下次提醒：用一句话提醒用户下次遇到类似决策时可以注意什么。",
                  "goalAlignment": {
                    "score": 0,
                    "level": "无长期目标/低度契合/中度契合/高度契合",
                    "bestOption": "最契合长期目标的候选方案名称",
                    "reason": "这次选择与长期目标的关系",
                    "optionAnalysis": [
                      { "option": "候选方案名称", "score": 0, "comment": "该方案与长期目标的关系" }
                    ]
                  }
                }
                """.formatted(
                fallback(decision.title(), "未填写标题"),
                fallback(decision.context(), "未填写背景"),
                fallback(decision.options(), "未填写候选方案"),
                fallback(decision.selectedOption(), "未标记最终选择"),
                fallback(decision.reason(), "未填写选择原因"),
                fallback(decision.tags(), "未填写标签"),
                fallback(decision.mood(), "未填写心情"),
                decision.urgency(),
                fallback(decision.satisfaction(), "未填写回测满意度"),
                fallback(decision.feedback(), "未填写回测反馈"),
                historySummary,
                fallback(goalContext, "未关联长期目标")
        );
    }

    private String buildGoalContext(AdviceGenerateRequest request, Long userId) {
        List<Goal> goals = resolveGoals(request, userId);
        if (goals.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (int index = 0; index < goals.size(); index++) {
            Goal goal = goals.get(index);
            builder.append("目标 ").append(index + 1).append("：\n")
                    .append("标题：").append(fallback(goal.getTitle(), "未填写")).append('\n')
                    .append("描述：").append(fallback(goal.getDescription(), "未填写")).append('\n')
                    .append("分类：").append(fallback(goal.getCategory(), "未填写")).append('\n')
                    .append("优先级：").append(fallback(goal.getPriority(), "MEDIUM")).append('\n')
                    .append("衡量方式：").append(fallback(goal.getMeasurement(), "未填写")).append('\n')
                    .append("预期完成日期：").append(goal.getTargetDate() == null ? "未设置" : goal.getTargetDate()).append('\n')
                    .append("当前进度：").append(goal.getProgress() == null ? 0 : goal.getProgress()).append("%\n");
        }
        return builder.toString();
    }

    private List<Goal> resolveGoals(AdviceGenerateRequest request, Long userId) {
        if (goalMapper == null || userId == null) {
            return List.of();
        }
        List<Long> goalIds = resolveGoalIds(request, userId);
        if (goalIds.isEmpty()) {
            return List.of();
        }
        List<Goal> goals = new ArrayList<>();
        for (Long goalId : goalIds) {
            Goal goal = goalMapper.selectOne(new LambdaQueryWrapper<Goal>()
                    .eq(Goal::getId, goalId)
                    .eq(Goal::getUserId, userId)
                    .last("LIMIT 1"));
            if (goal != null) {
                goals.add(goal);
            }
        }
        return goals;
    }

    private List<Long> resolveGoalIds(AdviceGenerateRequest request, Long userId) {
        List<Long> ids = new ArrayList<>();
        if (request.goalId() != null) {
            ids.add(request.goalId());
        }
        if (request.goalIds() != null) {
            request.goalIds().stream()
                    .filter(id -> id != null && id > 0)
                    .forEach(ids::add);
        }
        if (ids.isEmpty() && request.decisionId() != null) {
            if (decisionGoalMapper != null) {
                try {
                    decisionGoalMapper.selectList(new LambdaQueryWrapper<DecisionGoal>()
                                    .eq(DecisionGoal::getDecisionId, request.decisionId()))
                            .stream()
                            .map(DecisionGoal::getGoalId)
                            .filter(id -> id != null && id > 0)
                            .forEach(ids::add);
                } catch (DataAccessException exception) {
                    // Fall back to legacy decision.goal_id below.
                }
            }
            decisionMapper.findByIdAndUserId(request.decisionId(), userId)
                    .map(Decision::goalId)
                    .filter(id -> id != null && id > 0)
                    .ifPresent(ids::add);
        }
        return ids.stream().distinct().toList();
    }

    private String buildHistorySummary(AdviceGenerateRequest currentDecision, Long userId) {
        if (userId == null) {
            return "历史数据较少，本次建议主要基于当前决策信息。";
        }
        Long currentDecisionId = currentDecision.decisionId();
        List<String> currentTags = splitTags(currentDecision.tags());
        List<Decision> reviewedHistory = decisionMapper.selectList(new QueryWrapper<Decision>()
                        .eq("user_id", userId)
                        .eq("deleted", 0)
                        .eq("status", "reviewed")
                        .isNotNull("satisfaction")
                        .ne("satisfaction", "")
                        .ne(currentDecisionId != null, "id", currentDecisionId)
                        .orderByDesc("review_time", "create_time")
                        .last("LIMIT 100"))
                .stream()
                .filter(decision -> userId.equals(decision.userId()))
                .filter(decision -> currentDecisionId == null || !currentDecisionId.equals(decision.id()))
                .filter(decision -> "reviewed".equals(decision.status()))
                .filter(decision -> hasText(decision.satisfaction()))
                .toList();
        if (reviewedHistory.isEmpty()) {
            return "历史数据较少，本次建议主要基于当前决策信息。";
        }

        List<Decision> relatedHistory = reviewedHistory.stream()
                .filter(decision -> hasAnyTag(decision.tags(), currentTags))
                .toList();
        boolean fallbackToAllReviewed = relatedHistory.isEmpty();
        List<Decision> selectedHistory = fallbackToAllReviewed ? reviewedHistory : relatedHistory;
        Map<String, Integer> counts = satisfactionCounts(selectedHistory);
        List<Decision> topSimilar = selectedHistory.stream()
                .sorted(Comparator.comparingInt((Decision decision) -> relevanceScore(currentDecision, decision, currentTags)).reversed())
                .limit(5)
                .toList();

        StringBuilder builder = new StringBuilder();
        builder.append(fallbackToAllReviewed
                ? "同标签历史数据不足，使用当前用户全部已回测决策作为兜底统计。"
                : "同标签历史回测 " + selectedHistory.size() + " 条。");
        builder.append("\n统计：共 ").append(selectedHistory.size())
                .append(" 条；满意 ").append(counts.getOrDefault("满意", 0))
                .append(" 条，一般 ").append(counts.getOrDefault("一般", 0))
                .append(" 条，后悔 ").append(counts.getOrDefault("后悔", 0))
                .append(" 条。");
        builder.append("\n相似历史记录：");
        for (Decision decision : topSimilar) {
            builder.append("\n- ")
                    .append(fallback(decision.title(), "未命名决策"))
                    .append("；标签：").append(fallback(decision.tags(), "无"))
                    .append("；满意度：").append(decision.satisfaction())
                    .append("；反馈：").append(fallback(decision.feedback(), "无"));
        }
        return builder.toString();
    }

    private List<String> splitTags(String rawTags) {
        if (!hasText(rawTags)) {
            return List.of();
        }
        String[] values = rawTags.split("[,，、\\s]+");
        List<String> tags = new ArrayList<>();
        for (String value : values) {
            String tag = value.trim();
            if (!tag.isBlank()) {
                tags.add(tag);
            }
        }
        return tags;
    }

    private boolean hasAnyTag(String decisionTags, List<String> targetTags) {
        if (targetTags.isEmpty() || !hasText(decisionTags)) {
            return false;
        }
        for (String tag : targetTags) {
            if (decisionTags.contains(tag)) {
                return true;
            }
        }
        return false;
    }

    private Map<String, Integer> satisfactionCounts(List<Decision> decisions) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (Decision decision : decisions) {
            counts.merge(decision.satisfaction(), 1, Integer::sum);
        }
        return counts;
    }

    private int relevanceScore(AdviceGenerateRequest currentDecision, Decision historyDecision, List<String> currentTags) {
        int score = 0;
        for (String tag : currentTags) {
            if (hasText(historyDecision.tags()) && historyDecision.tags().contains(tag)) {
                score += 5;
            }
        }
        String currentTitle = fallback(currentDecision.title(), "");
        String historyTitle = fallback(historyDecision.title(), "");
        if (hasText(currentTitle) && hasText(historyTitle)) {
            if (currentTitle.contains(historyTitle) || historyTitle.contains(currentTitle)) {
                score += 8;
            }
            for (String token : currentTitle.split("[,，、\\s]+")) {
                if (token.length() >= 2 && historyTitle.contains(token)) {
                    score += 2;
                }
            }
        }
        return score;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String fallback(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String extractFirstContent(String rawJson) {
        try {
            JsonNode root = objectMapper.readTree(rawJson);
            JsonNode content = root.path("choices").path(0).path("message").path("content");
            return content.isTextual() ? content.asText() : "";
        } catch (Exception exception) {
            return "";
        }
    }

    private String cleanJsonContent(String content) {
        String cleaned = content.trim();
        if (cleaned.startsWith("```")) {
            cleaned = cleaned.replaceFirst("^```(?:json)?\\s*", "").replaceFirst("\\s*```$", "").trim();
        }
        int start = cleaned.indexOf('{');
        int end = cleaned.lastIndexOf('}');
        if (start < 0 || end <= start) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI 建议生成失败，请稍后重试");
        }
        return cleaned.substring(start, end + 1);
    }

    private DecisionAdviceResponse normalizeAdvice(DecisionAdviceResponse advice) {
        List<DecisionAdviceResponse.OptionAdvice> options = advice.options() == null
                ? List.of()
                : advice.options().stream()
                .map(this::normalizeOptionAdvice)
                .toList();
        return new DecisionAdviceResponse(
                fallback(advice.overallAdvice(), "暂无整体建议"),
                options,
                fallback(advice.reminder(), "最终选择前，可以再补充关键约束和最担心的风险。"),
                fallback(advice.summary(), ""),
                fallback(advice.factors(), ""),
                fallback(advice.risks(), ""),
                advice.improvements() == null ? List.of() : advice.improvements(),
                fallback(advice.nextReminder(), ""),
                normalizeGoalAlignment(advice.goalAlignment())
        );
    }

    private DecisionAdviceResponse.GoalAlignmentDTO normalizeGoalAlignment(DecisionAdviceResponse.GoalAlignmentDTO goalAlignment) {
        if (goalAlignment == null) {
            return null;
        }
        return new DecisionAdviceResponse.GoalAlignmentDTO(
                goalAlignment.score() == null ? 0 : Math.max(0, Math.min(100, goalAlignment.score())),
                fallback(goalAlignment.level(), ""),
                fallback(goalAlignment.bestOption(), ""),
                fallback(goalAlignment.reason(), ""),
                goalAlignment.optionAnalysis() == null ? List.of() : goalAlignment.optionAnalysis()
        );
    }

    private DecisionAdviceResponse.OptionAdvice normalizeOptionAdvice(DecisionAdviceResponse.OptionAdvice option) {
        if (option == null) {
            return new DecisionAdviceResponse.OptionAdvice("未命名方案", List.of(), List.of(), List.of(), "暂无", "暂无");
        }
        return new DecisionAdviceResponse.OptionAdvice(
                fallback(option.name(), "未命名方案"),
                option.pros() == null ? List.of() : option.pros(),
                option.cons() == null ? List.of() : option.cons(),
                option.risks() == null ? List.of() : option.risks(),
                fallback(option.bestFor(), "暂无"),
                fallback(option.suggestion(), "暂无")
        );
    }

    private String escapeJson(String value) {
        return fallback(value, "")
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n")
                .replace("\t", "\\t");
    }

    private record AiSettings(String apiKey, String baseUrl, String model) {
    }
}
