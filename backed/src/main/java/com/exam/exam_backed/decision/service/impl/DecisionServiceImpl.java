package com.exam.exam_backed.decision.service.impl;

import com.exam.exam_backed.common.BusinessException;
import com.exam.exam_backed.common.ErrorCode;
import com.exam.exam_backed.decision.vo.DecisionDetail;
import com.exam.exam_backed.decision.vo.DecisionOption;
import com.exam.exam_backed.decision.Decision;
import com.exam.exam_backed.decision.dto.DecisionCreateRequest;
import com.exam.exam_backed.decision.dto.DecisionReviewRequest;
import com.exam.exam_backed.decision.mapper.DecisionMapper;
import com.exam.exam_backed.decision.service.DecisionService;
import com.exam.exam_backed.decision.vo.DecisionDashboard;
import com.exam.exam_backed.decision.vo.DecisionSummary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DecisionServiceImpl implements DecisionService {
    private static final String STATUS_PENDING = "pending";
    private static final String STATUS_REVIEWED = "reviewed";
    private static final String DECISION_NOT_FOUND = "决策记录不存在";
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
    public List<Decision> search(Long userId, String keyword, String tag, String status, int limit) {
        int normalizedLimit = Math.min(Math.max(limit, 1), 100);
        return decisionMapper.searchByUserId(
                userId,
                normalizeText(keyword),
                normalizeText(tag),
                normalizeStatus(status),
                normalizedLimit
        );
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
    public DecisionDetail detail(Long userId, Long decisionId) {
        Decision decision = findExistingDecision(userId, decisionId);
        ParsedOptions parsedOptions = parseOptions(decision.options());
        return new DecisionDetail(
                decision.id(),
                decision.title(),
                decision.context(),
                parsedOptions.options(),
                parsedOptions.finalChoice(),
                decision.reason(),
                decision.status(),
                decision.createTime()
        );
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizeStatus(String status) {
        String value = normalizeText(status);
        if (value == null) {
            return null;
        }
        if (STATUS_PENDING.equals(value) || STATUS_REVIEWED.equals(value)) {
            return value;
        }
        throw new BusinessException(ErrorCode.PARAM_ERROR, "决策状态筛选值不正确");
    }

    @Override
    @Transactional
    public Decision review(Long userId, Long decisionId, DecisionReviewRequest request) {
        findExistingDecision(userId, decisionId);
        decisionMapper.updateReview(decisionId, userId, request.satisfaction(), request.feedback(), STATUS_REVIEWED);
        return decisionMapper.findByIdAndUserId(decisionId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PARAM_ERROR, DECISION_NOT_FOUND));
    }

    @Override
    @Transactional
    public void delete(Long userId, Long decisionId) {
        if (decisionMapper.softDelete(decisionId, userId) == 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, DECISION_NOT_FOUND);
        }
    }

    private Decision findExistingDecision(Long userId, Long decisionId) {
        return decisionMapper.findByIdAndUserId(decisionId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PARAM_ERROR, DECISION_NOT_FOUND));
    }

    private ParsedOptions parseOptions(String rawOptions) {
        if (rawOptions == null || rawOptions.isBlank()) {
            return new ParsedOptions(List.of(), "");
        }
        if (rawOptions.trim().startsWith("{")) {
            ParsedOptions parsedOptions = parseJsonOptions(rawOptions);
            if (!parsedOptions.options().isEmpty()) {
                return parsedOptions;
            }
        }
        List<DecisionOption> legacyOptions = new ArrayList<>();
        String[] titles = rawOptions.split(",");
        for (int i = 0; i < titles.length; i++) {
            String title = titles[i].trim();
            if (!title.isEmpty()) {
                legacyOptions.add(new DecisionOption("legacy_" + i, title, List.of()));
            }
        }
        String finalChoice = legacyOptions.isEmpty() ? "" : legacyOptions.get(0).title();
        return new ParsedOptions(legacyOptions, finalChoice);
    }

    private ParsedOptions parseJsonOptions(String rawOptions) {
        String selectedId = extractStringField(rawOptions, "selectedId");
        int itemsField = rawOptions.indexOf("\"items\"");
        if (itemsField < 0) {
            return new ParsedOptions(List.of(), "");
        }
        int arrayStart = rawOptions.indexOf('[', itemsField);
        if (arrayStart < 0) {
            return new ParsedOptions(List.of(), "");
        }
        int arrayEnd = findMatching(rawOptions, arrayStart, '[', ']');
        if (arrayEnd < 0) {
            return new ParsedOptions(List.of(), "");
        }
        List<DecisionOption> options = parseOptionArray(rawOptions.substring(arrayStart + 1, arrayEnd));
        return new ParsedOptions(options, findSelectedTitle(options, selectedId));
    }

    private List<DecisionOption> parseOptionArray(String rawArray) {
        List<DecisionOption> options = new ArrayList<>();
        int index = 0;
        while (index < rawArray.length()) {
            int objectStart = rawArray.indexOf('{', index);
            if (objectStart < 0) {
                break;
            }
            int objectEnd = findMatching(rawArray, objectStart, '{', '}');
            if (objectEnd < 0) {
                break;
            }
            String rawObject = rawArray.substring(objectStart, objectEnd + 1);
            String title = extractStringField(rawObject, "title").trim();
            if (!title.isEmpty()) {
                options.add(new DecisionOption(extractStringField(rawObject, "id"), title, parseChildren(rawObject)));
            }
            index = objectEnd + 1;
        }
        return options;
    }

    private List<DecisionOption> parseChildren(String rawObject) {
        int childrenField = rawObject.indexOf("\"children\"");
        if (childrenField < 0) {
            return List.of();
        }
        int arrayStart = rawObject.indexOf('[', childrenField);
        if (arrayStart < 0) {
            return List.of();
        }
        int arrayEnd = findMatching(rawObject, arrayStart, '[', ']');
        if (arrayEnd < 0) {
            return List.of();
        }
        return parseOptionArray(rawObject.substring(arrayStart + 1, arrayEnd));
    }

    private String extractStringField(String rawObject, String fieldName) {
        int fieldStart = rawObject.indexOf("\"" + fieldName + "\"");
        if (fieldStart < 0) {
            return "";
        }
        int colon = rawObject.indexOf(':', fieldStart);
        if (colon < 0) {
            return "";
        }
        int quoteStart = rawObject.indexOf('"', colon + 1);
        if (quoteStart < 0) {
            return "";
        }
        StringBuilder value = new StringBuilder();
        boolean escaping = false;
        for (int i = quoteStart + 1; i < rawObject.length(); i++) {
            char current = rawObject.charAt(i);
            if (escaping) {
                value.append(current);
                escaping = false;
                continue;
            }
            if (current == '\\') {
                escaping = true;
                continue;
            }
            if (current == '"') {
                return value.toString();
            }
            value.append(current);
        }
        return "";
    }

    private int findMatching(String value, int openIndex, char openChar, char closeChar) {
        int depth = 0;
        boolean inString = false;
        boolean escaping = false;
        for (int i = openIndex; i < value.length(); i++) {
            char current = value.charAt(i);
            if (escaping) {
                escaping = false;
                continue;
            }
            if (current == '\\') {
                escaping = true;
                continue;
            }
            if (current == '"') {
                inString = !inString;
                continue;
            }
            if (inString) {
                continue;
            }
            if (current == openChar) {
                depth++;
                continue;
            }
            if (current == closeChar) {
                depth--;
                if (depth == 0) {
                    return i;
                }
            }
        }
        return -1;
    }

    private String findSelectedTitle(List<DecisionOption> options, String selectedId) {
        for (DecisionOption option : options) {
            if (option.id().equals(selectedId)) {
                return option.title();
            }
        }
        return "";
    }

    private record ParsedOptions(List<DecisionOption> options, String finalChoice) {
    }
}
