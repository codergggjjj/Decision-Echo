package com.exam.exam_backed.advice.controller;

import com.exam.exam_backed.advice.dto.AdviceGenerateRequest;
import com.exam.exam_backed.advice.service.AdviceService;
import com.exam.exam_backed.advice.vo.DecisionAdviceResponse;
import com.exam.exam_backed.auth.service.TokenService;
import com.exam.exam_backed.common.BusinessException;
import com.exam.exam_backed.common.ErrorCode;
import com.exam.exam_backed.common.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/advice")
public class AdviceController {
    private final AdviceService adviceService;
    private final TokenService tokenService;

    public AdviceController(AdviceService adviceService, TokenService tokenService) {
        this.adviceService = adviceService;
        this.tokenService = tokenService;
    }

    @PostMapping("/generate")
    public Result<DecisionAdviceResponse> generate(HttpServletRequest request, @RequestBody AdviceGenerateRequest body) {
        currentUserId(request);
        return Result.success(adviceService.generate(body));
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
