package com.exam.exam_backed.analysis.controller;

import com.exam.exam_backed.analysis.service.AnalysisService;
import com.exam.exam_backed.analysis.vo.MoodSatisfactionResponse;
import com.exam.exam_backed.analysis.vo.SatisfactionPieResponse;
import com.exam.exam_backed.analysis.vo.TagBarResponse;
import com.exam.exam_backed.analysis.vo.TrendLineResponse;
import com.exam.exam_backed.auth.service.TokenService;
import com.exam.exam_backed.common.BusinessException;
import com.exam.exam_backed.common.ErrorCode;
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
    private final TokenService tokenService;

    public AnalysisController(AnalysisService analysisService, TokenService tokenService) {
        this.analysisService = analysisService;
        this.tokenService = tokenService;
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
        return tokenService.validate(extractToken(request))
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "请先登录"))
                .id();
    }

    private String extractToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        return authorization;
    }
}
