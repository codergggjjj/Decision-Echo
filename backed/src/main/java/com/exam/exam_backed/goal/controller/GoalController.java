package com.exam.exam_backed.goal.controller;

import com.exam.exam_backed.auth.service.AuthSessionService;
import com.exam.exam_backed.common.Result;
import com.exam.exam_backed.goal.dto.GoalRecommendRequest;
import com.exam.exam_backed.goal.dto.GoalRequest;
import com.exam.exam_backed.goal.service.GoalService;
import com.exam.exam_backed.goal.vo.GoalDetailVO;
import com.exam.exam_backed.goal.vo.GoalListItemVO;
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
@RequestMapping("/api/goals")
public class GoalController {
    private final GoalService goalService;
    private final AuthSessionService authSessionService;

    public GoalController(GoalService goalService, AuthSessionService authSessionService) {
        this.goalService = goalService;
        this.authSessionService = authSessionService;
    }

    @GetMapping
    public Result<List<GoalListItemVO>> list(@RequestParam(required = false) String status) {
        return Result.success(goalService.list(currentUserId(), status));
    }

    @GetMapping("/{id}")
    public Result<GoalDetailVO> detail(@PathVariable Long id) {
        return Result.success(goalService.detail(currentUserId(), id));
    }

    @PostMapping
    public Result<GoalDetailVO> create(@Valid @RequestBody GoalRequest request) {
        return Result.success(goalService.create(currentUserId(), request));
    }

    @PutMapping("/{id}")
    public Result<GoalDetailVO> update(@PathVariable Long id, @Valid @RequestBody GoalRequest request) {
        return Result.success(goalService.update(currentUserId(), id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        goalService.delete(currentUserId(), id);
        return Result.success(null);
    }

    @PostMapping("/recommend")
    public Result<List<GoalListItemVO>> recommend(@RequestBody GoalRecommendRequest request) {
        return Result.success(goalService.recommendGoalsByTags(currentUserId(), request.tags()));
    }

    private Long currentUserId() {
        return authSessionService.currentUserId();
    }
}
