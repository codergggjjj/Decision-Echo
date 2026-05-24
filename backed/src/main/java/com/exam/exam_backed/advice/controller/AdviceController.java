package com.exam.exam_backed.advice.controller;

import com.exam.exam_backed.advice.dto.AdviceGenerateRequest;
import com.exam.exam_backed.advice.service.AdviceService;
import com.exam.exam_backed.advice.vo.DecisionAdviceResponse;
import com.exam.exam_backed.auth.service.AuthSessionService;
import com.exam.exam_backed.common.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/advice")
public class AdviceController {
    private final AdviceService adviceService;
    private final AuthSessionService authSessionService;

    public AdviceController(AdviceService adviceService, AuthSessionService authSessionService) {
        this.adviceService = adviceService;
        this.authSessionService = authSessionService;
    }

    @PostMapping("/generate")
    public Result<DecisionAdviceResponse> generate(HttpServletRequest request, @RequestBody AdviceGenerateRequest body) {
        currentUserId(request);
        return Result.success(adviceService.generate(body));
    }

    private Long currentUserId(HttpServletRequest request) {
        return authSessionService.currentUserId();
    }
}
