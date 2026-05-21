package com.exam.exam_backed.analysis.vo;

import java.util.List;

public record TrendLineResponse(
        int total,
        List<String> labels,
        List<Integer> counts,
        String granularity,
        String selectedMonth
) {
}
