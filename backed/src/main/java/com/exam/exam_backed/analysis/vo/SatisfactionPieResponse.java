package com.exam.exam_backed.analysis.vo;

import java.util.List;

public record SatisfactionPieResponse(int total, List<SatisfactionPieItem> items) {
}
