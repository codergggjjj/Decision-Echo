package com.exam.exam_backed.decision.controller;

import com.exam.exam_backed.auth.service.TokenService;
import com.exam.exam_backed.common.BusinessException;
import com.exam.exam_backed.common.ErrorCode;
import com.exam.exam_backed.common.Result;
import com.exam.exam_backed.decision.Decision;
import com.exam.exam_backed.decision.dto.DecisionCreateRequest;
import com.exam.exam_backed.decision.dto.DecisionReviewRequest;
import com.exam.exam_backed.decision.service.DecisionService;
import com.exam.exam_backed.decision.vo.DecisionDashboard;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/decisions")
public class DecisionController {
    private final DecisionService decisionService;
    private final TokenService tokenService;

    public DecisionController(DecisionService decisionService, TokenService tokenService) {
        this.decisionService = decisionService;
        this.tokenService = tokenService;
    }

    @GetMapping("/dashboard")
    public Result<DecisionDashboard> dashboard(HttpServletRequest request) {
        return Result.success(decisionService.dashboard(currentUserId(request)));
    }

    @GetMapping
    public Result<List<Decision>> recent(HttpServletRequest request, @RequestParam(defaultValue = "20") int limit) {
        return Result.success(decisionService.recent(currentUserId(request), limit));
    }

    @PostMapping
    public Result<Decision> create(HttpServletRequest request, @Valid @RequestBody DecisionCreateRequest body) {
        return Result.success(decisionService.create(currentUserId(request), body));
    }

    @PutMapping("/{id}/review")
    public Result<Decision> review(
            HttpServletRequest request,
            @PathVariable Long id,
            @Valid @RequestBody DecisionReviewRequest body
    ) {
        return Result.success(decisionService.review(currentUserId(request), id, body));
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
