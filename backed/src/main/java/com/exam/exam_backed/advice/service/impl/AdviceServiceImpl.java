package com.exam.exam_backed.advice.service.impl;

import com.exam.exam_backed.advice.dto.AdviceGenerateRequest;
import com.exam.exam_backed.advice.service.AdviceService;
import com.exam.exam_backed.advice.vo.DecisionAdviceResponse;
import com.exam.exam_backed.common.BusinessException;
import com.exam.exam_backed.common.ErrorCode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

@Service
public class AdviceServiceImpl implements AdviceService {
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String baseUrl;
    private final String model;

    public AdviceServiceImpl(
            @Value("${advice.ai.api-key:}") String apiKey,
            @Value("${advice.ai.base-url:https://dashscope.aliyuncs.com/compatible-mode/v1}") String baseUrl,
            @Value("${advice.ai.model:qwen-plus-latest}") String model
    ) {
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.model = model;
        this.objectMapper = new ObjectMapper();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(8))
                .build();
    }

    @Override
    public DecisionAdviceResponse generate(AdviceGenerateRequest request) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI 服务未配置，请先设置 API Key");
        }
        return requestAdvice(request);
    }

    private DecisionAdviceResponse requestAdvice(AdviceGenerateRequest decision) {
        try {
            String requestBody = buildRequestBody(decision);
            HttpRequest request = HttpRequest.newBuilder(chatCompletionsUri())
                    .timeout(Duration.ofSeconds(30))
                    .header("Authorization", "Bearer " + apiKey)
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

    private URI chatCompletionsUri() {
        String normalizedBaseUrl = baseUrl == null ? "" : baseUrl.trim();
        if (normalizedBaseUrl.endsWith("/")) {
            normalizedBaseUrl = normalizedBaseUrl.substring(0, normalizedBaseUrl.length() - 1);
        }
        return URI.create(normalizedBaseUrl + "/chat/completions");
    }

    private String buildRequestBody(AdviceGenerateRequest decision) {
        boolean reviewMode = isReviewMode(decision);
        return """
                {"model":"%s","temperature":0.3,"response_format":{"type":"json_object"},"messages":[{"role":"system","content":"%s"},{"role":"user","content":"%s"}]}
                """.formatted(
                escapeJson(model),
                escapeJson(reviewMode
                        ? "你是一个理性、温和、客观的个人决策复盘助手。你必须只输出合法 JSON，不要输出 Markdown，不要输出 JSON 以外的解释。"
                        : "你是一个理性、温和、客观的个人决策分析助手。你必须只输出合法 JSON，不要输出 Markdown，不要输出 JSON 以外的解释。"),
                escapeJson(reviewMode ? buildReviewPrompt(decision) : buildCreatePrompt(decision))
        ).trim();
    }

    private boolean isReviewMode(AdviceGenerateRequest decision) {
        return "review".equalsIgnoreCase(fallback(decision.mode(), ""));
    }

    private String buildCreatePrompt(AdviceGenerateRequest decision) {
        return """
                请根据用户提供的决策背景和候选方案，对每个候选方案进行利弊分析。

                重要要求：
                1. 不要替用户直接做最终决定。
                2. 不要使用绝对化语气，例如“必须选”“一定不要选”。
                3. 不要编造用户没有提供的信息。
                4. 如果信息不足，请明确指出还需要补充哪些信息。
                5. 分析要简洁、具体、可执行。
                6. 语气温和，适合普通用户阅读。
                7. 输出必须是合法 JSON，不要输出 Markdown，不要输出 JSON 以外的解释。

                用户决策信息：

                决策标题：%s
                决策背景：%s
                决策标签：%s
                当前心情：%s
                紧急程度：%s

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
                  "reminder": "最终选择前需要注意的一句话"
                }
                """.formatted(
                fallback(decision.title(), "未填写标题"),
                fallback(decision.context(), "未填写背景"),
                fallback(decision.tags(), "未填写标签"),
                fallback(decision.mood(), "未填写心情"),
                decision.urgency(),
                fallback(decision.options(), "未填写候选方案")
        );
    }

    private String buildReviewPrompt(AdviceGenerateRequest decision) {
        return """
                请根据用户提供的决策记录、回测结果和历史满意度情况，生成一份结构清晰、语气温和、可执行的复盘建议。

                重要要求：
                1. 不要替用户做绝对判断。
                2. 不要使用“你必须”“你一定错了”“绝对应该”等强制或责备语气。
                3. 不要编造用户没有提供的信息。
                4. 如果信息不足，请基于已有信息谨慎分析。
                5. 建议要具体、简洁、可执行。
                6. 不要输出医学、法律、金融等高风险专业建议。
                7. 总字数控制在 300 字以内。
                8. 必须严格按照指定 JSON 格式输出。
                9. 不要输出 Markdown。
                10. 不要输出 JSON 以外的任何解释。

                用户决策信息如下：

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
                用户历史满意度概况：%s

                请严格返回以下 JSON 格式：

                {
                  "summary": "决策概括：用 1-2 句话概括这次决策的核心内容和结果。",
                  "factors": "影响因素：分析当时影响用户选择的主要因素，例如情绪、时间压力、信息充分度、收益预期等。",
                  "risks": "风险问题：指出这次决策中可能存在的不足、风险或可以反思的地方，语气要温和，不要责备。",
                  "improvements": [
                    "改进建议1：给出一条具体、可执行的建议。",
                    "改进建议2：给出一条具体、可执行的建议。",
                    "改进建议3：给出一条具体、可执行的建议。"
                  ],
                  "nextReminder": "下次提醒：用一句话提醒用户下次遇到类似决策时可以注意什么。"
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
                fallback(decision.historySummary(), "暂无历史满意度概况")
        );
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
                fallback(advice.nextReminder(), "")
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
}
