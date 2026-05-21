package com.exam.exam_backed.analysis.vo;

import java.util.Map;

public record MoodSatisfactionItem(String mood, int total, Map<String, Integer> satisfaction) {
}
