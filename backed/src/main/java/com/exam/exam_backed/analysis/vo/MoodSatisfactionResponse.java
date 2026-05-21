package com.exam.exam_backed.analysis.vo;

import java.util.List;

public record MoodSatisfactionResponse(int total, List<MoodSatisfactionItem> items) {
}
