package com.exam.exam_backed.goal.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record GoalRequest(
        @Size(max = 100, message = "目标标题长度不能超过100")
        String title,
        String description,
        @Size(max = 50, message = "目标分类长度不能超过50")
        String category,
        String priority,
        String status,
        LocalDate targetDate,
        @Size(max = 255, message = "衡量方式长度不能超过255")
        String measurement,
        @Min(value = 0, message = "目标进度不能小于0")
        @Max(value = 100, message = "目标进度不能大于100")
        Integer progress,
        List<String> tags
) {
}
