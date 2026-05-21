package com.exam.exam_backed.analysis.vo;

import java.util.List;

public record TagBarResponse(int total, List<TagBarItem> items) {
}
