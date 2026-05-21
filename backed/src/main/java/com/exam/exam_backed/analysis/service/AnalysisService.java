package com.exam.exam_backed.analysis.service;

import com.exam.exam_backed.analysis.vo.SatisfactionPieResponse;
import com.exam.exam_backed.analysis.vo.TagBarResponse;
import com.exam.exam_backed.analysis.vo.TrendLineResponse;

public interface AnalysisService {
    SatisfactionPieResponse satisfactionPie(Long userId, String tag, String mood);

    TrendLineResponse trendLine(Long userId, String month);

    TagBarResponse tagBar(Long userId);
}
