package com.exam.exam_backed.analysis.controller;

import com.exam.exam_backed.analysis.service.AnalysisService;
import com.exam.exam_backed.analysis.vo.MoodSatisfactionResponse;
import com.exam.exam_backed.analysis.vo.SatisfactionPieResponse;
import com.exam.exam_backed.analysis.vo.TagBarResponse;
import com.exam.exam_backed.analysis.vo.TrendLineResponse;
import com.exam.exam_backed.auth.service.AuthSessionService;
import com.exam.exam_backed.common.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analysis")
public class AnalysisController {
    private final AnalysisService analysisService;
    private final AuthSessionService authSessionService;

    public AnalysisController(AnalysisService analysisService, AuthSessionService authSessionService) {
        this.analysisService = analysisService;
        this.authSessionService = authSessionService;
    }

    @GetMapping("/satisfaction-pie")
    public Result<SatisfactionPieResponse> satisfactionPie(
            HttpServletRequest request,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) String mood
    ) {
        return Result.success(analysisService.satisfactionPie(currentUserId(request), tag, mood));
    }

    @GetMapping("/trend-line")
    public Result<TrendLineResponse> trendLine(
            HttpServletRequest request,
            @RequestParam(required = false) String month
    ) {
        return Result.success(analysisService.trendLine(currentUserId(request), month));
    }

    @GetMapping("/mood-satisfaction")
    public Result<MoodSatisfactionResponse> moodSatisfaction(HttpServletRequest request) {
        return Result.success(analysisService.moodSatisfaction(currentUserId(request)));
    }

    @GetMapping("/tag-bar")
    public Result<TagBarResponse> tagBar(HttpServletRequest request) {
        return Result.success(analysisService.tagBar(currentUserId(request)));
    }

    private Long currentUserId(HttpServletRequest request) {
        return authSessionService.currentUserId();
    }
}
