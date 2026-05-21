package com.exam.exam_backed.analysis.service.impl;

import com.exam.exam_backed.analysis.service.AnalysisService;
import com.exam.exam_backed.analysis.vo.SatisfactionPieItem;
import com.exam.exam_backed.analysis.vo.SatisfactionPieResponse;
import com.exam.exam_backed.analysis.vo.TagBarItem;
import com.exam.exam_backed.analysis.vo.TagBarResponse;
import com.exam.exam_backed.analysis.vo.TrendLineResponse;
import com.exam.exam_backed.decision.mapper.DecisionMapper;
import com.exam.exam_backed.decision.mapper.DecisionMapper.TrendCount;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.regex.Pattern;

@Service
public class AnalysisServiceImpl implements AnalysisService {
    private static final List<String> SATISFACTION_LABELS = List.of("满意", "一般", "后悔");
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");
    private static final Pattern TAG_SEPARATOR = Pattern.compile("[,，]");
    private final DecisionMapper decisionMapper;

    public AnalysisServiceImpl(DecisionMapper decisionMapper) {
        this.decisionMapper = decisionMapper;
    }

    @Override
    public SatisfactionPieResponse satisfactionPie(Long userId, String tag, String mood) {
        String normalizedTag = normalizeText(tag);
        String normalizedMood = normalizeText(mood);
        List<SatisfactionPieItem> items = SATISFACTION_LABELS.stream()
                .map(label -> new SatisfactionPieItem(
                        label,
                        decisionMapper.countReviewedBySatisfactionAndFilters(userId, label, normalizedTag, normalizedMood)
                ))
                .toList();
        int total = items.stream().mapToInt(SatisfactionPieItem::value).sum();
        return new SatisfactionPieResponse(total, items);
    }

    @Override
    public TrendLineResponse trendLine(Long userId, String month) {
        String normalizedMonth = normalizeText(month);
        if (normalizedMonth != null) {
            return dailyTrend(userId, YearMonth.parse(normalizedMonth, MONTH_FORMATTER));
        }
        return monthlyTrend(userId, LocalDate.now().getYear());
    }

    @Override
    public TagBarResponse tagBar(Long userId) {
        List<TagBarItem> items = decisionMapper.findTagsByUserId(userId)
                .stream()
                .flatMap(tags -> TAG_SEPARATOR.splitAsStream(tags))
                .map(this::normalizeText)
                .filter(tag -> tag != null)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.summingInt(tag -> 1)))
                .entrySet()
                .stream()
                .sorted(Comparator
                        .<Map.Entry<String, Integer>>comparingInt(Map.Entry::getValue)
                        .reversed()
                        .thenComparing(Map.Entry::getKey))
                .map(entry -> new TagBarItem(entry.getKey(), entry.getValue()))
                .toList();
        int total = items.stream().mapToInt(TagBarItem::value).sum();
        return new TagBarResponse(total, items);
    }

    private TrendLineResponse monthlyTrend(Long userId, int year) {
        LocalDateTime start = LocalDate.of(year, 1, 1).atStartOfDay();
        LocalDateTime end = start.plusYears(1);
        Map<String, Integer> countMap = decisionMapper.countCreatedByMonth(userId, start, end)
                .stream()
                .collect(Collectors.toMap(TrendCount::label, TrendCount::count));
        List<String> labels = IntStream.rangeClosed(1, 12)
                .mapToObj(month -> "%04d-%02d".formatted(year, month))
                .toList();
        List<Integer> counts = labels.stream()
                .map(label -> countMap.getOrDefault(label, 0))
                .toList();
        return new TrendLineResponse(sum(counts), labels, counts, "month", null);
    }

    private TrendLineResponse dailyTrend(Long userId, YearMonth selectedMonth) {
        LocalDateTime start = selectedMonth.atDay(1).atStartOfDay();
        LocalDateTime end = start.plusMonths(1);
        Map<String, Integer> countMap = decisionMapper.countCreatedByDay(userId, start, end)
                .stream()
                .collect(Collectors.toMap(TrendCount::label, TrendCount::count));
        List<String> labels = IntStream.rangeClosed(1, selectedMonth.lengthOfMonth())
                .mapToObj(day -> "%02d日".formatted(day))
                .toList();
        List<Integer> counts = IntStream.rangeClosed(1, selectedMonth.lengthOfMonth())
                .mapToObj(day -> countMap.getOrDefault("%02d".formatted(day), 0))
                .toList();
        return new TrendLineResponse(sum(counts), labels, counts, "day", selectedMonth.format(MONTH_FORMATTER));
    }

    private int sum(List<Integer> counts) {
        return counts.stream().mapToInt(Integer::intValue).sum();
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
