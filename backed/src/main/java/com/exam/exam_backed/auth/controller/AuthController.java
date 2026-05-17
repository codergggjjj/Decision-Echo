package com.exam.exam_backed.auth.controller;

import com.exam.exam_backed.auth.dto.LoginRequest;
import com.exam.exam_backed.auth.service.AuthService;
import com.exam.exam_backed.auth.service.CaptchaService;
import com.exam.exam_backed.auth.service.TokenService;
import com.exam.exam_backed.auth.vo.CaptchaResponse;
import com.exam.exam_backed.auth.vo.LoginResponse;
import com.exam.exam_backed.auth.vo.UserVO;
import com.exam.exam_backed.common.BusinessException;
import com.exam.exam_backed.common.ErrorCode;
import com.exam.exam_backed.common.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final CaptchaService captchaService;
    private final TokenService tokenService;

    public AuthController(AuthService authService, CaptchaService captchaService, TokenService tokenService) {
        this.authService = authService;
        this.captchaService = captchaService;
        this.tokenService = tokenService;
    }

    @GetMapping("/captcha")
    public Result<CaptchaResponse> captcha() {
        return Result.success(captchaService.createCaptcha());
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(authService.login(request));
    }

    @GetMapping("/me")
    public Result<UserVO> me(HttpServletRequest request) {
        return Result.success(tokenService.validate(extractToken(request))
                .map(UserVO::from)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "请先登录")));
    }

    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        tokenService.revoke(extractToken(request));
        return Result.success(null);
    }

    private String extractToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        return authorization;
    }
}
