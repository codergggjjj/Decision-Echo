package com.exam.exam_backed.decision.controller;

import com.exam.exam_backed.auth.service.AuthSessionService;
import com.exam.exam_backed.common.Result;
import com.exam.exam_backed.decision.Decision;
import com.exam.exam_backed.decision.dto.DecisionCreateRequest;
import com.exam.exam_backed.decision.dto.DecisionReviewRequest;
import com.exam.exam_backed.decision.service.DecisionService;
import com.exam.exam_backed.decision.vo.DecisionDashboard;
import com.exam.exam_backed.decision.vo.DecisionDetail;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
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
    private final AuthSessionService authSessionService;

    public DecisionController(DecisionService decisionService, AuthSessionService authSessionService) {
        this.decisionService = decisionService;
        this.authSessionService = authSessionService;
    }

    @GetMapping("/dashboard")
    public Result<DecisionDashboard> dashboard(HttpServletRequest request) {
        return Result.success(decisionService.dashboard(currentUserId(request)));
    }

    @GetMapping
    public Result<List<Decision>> search(
            HttpServletRequest request,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "50") int limit
    ) {
        return Result.success(decisionService.search(currentUserId(request), keyword, tag, status, limit));
    }

    @GetMapping("/{id}")
    public Result<DecisionDetail> detail(HttpServletRequest request, @PathVariable Long id) {
        return Result.success(decisionService.detail(currentUserId(request), id));
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

    @DeleteMapping("/{id}")
    public Result<Void> delete(HttpServletRequest request, @PathVariable Long id) {
        decisionService.delete(currentUserId(request), id);
        return Result.success(null);
    }

    private Long currentUserId(HttpServletRequest request) {
        return authSessionService.currentUserId();
    }
}
