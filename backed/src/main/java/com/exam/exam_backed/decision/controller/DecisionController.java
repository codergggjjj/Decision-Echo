package com.exam.exam_backed.decision.controller;

import com.exam.exam_backed.auth.service.AuthSessionService;
import com.exam.exam_backed.common.Result;
import com.exam.exam_backed.decision.Decision;
import com.exam.exam_backed.decision.dto.DecisionCreateRequest;
import com.exam.exam_backed.decision.dto.DecisionReviewRequest;
import com.exam.exam_backed.decision.service.DecisionService;
import com.exam.exam_backed.decision.vo.DecisionDashboard;
import com.exam.exam_backed.decision.vo.DecisionDetail;
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
    public Result<DecisionDashboard> dashboard() {
        return Result.success(decisionService.dashboard(currentUserId()));
    }

    @GetMapping
    public Result<List<Decision>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "50") int limit
    ) {
        return Result.success(decisionService.search(currentUserId(), keyword, tag, status, limit));
    }

    @GetMapping("/{id}")
    public Result<DecisionDetail> detail(@PathVariable Long id) {
        return Result.success(decisionService.detail(currentUserId(), id));
    }

    @GetMapping("/goal/{goalId}")
    public Result<List<Decision>> byGoal(@PathVariable Long goalId) {
        return Result.success(decisionService.getDecisionsByGoalId(currentUserId(), goalId));
    }

    @PostMapping
    public Result<Decision> create(@Valid @RequestBody DecisionCreateRequest body) {
        return Result.success(decisionService.create(currentUserId(), body));
    }

    @PutMapping("/{id}/review")
    public Result<Decision> review(
            @PathVariable Long id,
            @Valid @RequestBody DecisionReviewRequest body
    ) {
        return Result.success(decisionService.review(currentUserId(), id, body));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        decisionService.delete(currentUserId(), id);
        return Result.success(null);
    }

    private Long currentUserId() {
        return authSessionService.currentUserId();
    }
}
