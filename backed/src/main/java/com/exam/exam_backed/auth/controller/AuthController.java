package com.exam.exam_backed.auth.controller;

import com.exam.exam_backed.auth.dto.LoginRequest;
import com.exam.exam_backed.auth.dto.PasswordChangeRequest;
import com.exam.exam_backed.auth.dto.ProfileUpdateRequest;
import com.exam.exam_backed.auth.dto.RegisterRequest;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

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

    @PostMapping("/register")
    public Result<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        return Result.success(authService.register(request));
    }

    @GetMapping("/me")
    public Result<UserVO> me(HttpServletRequest request) {
        return Result.success(authService.currentUser(currentUserId(request)));
    }

    @PutMapping("/profile")
    public Result<UserVO> updateProfile(HttpServletRequest request, @Valid @RequestBody ProfileUpdateRequest body) {
        return Result.success(authService.updateProfile(currentUserId(request), body));
    }

    @PostMapping("/avatar")
    public Result<Map<String, String>> uploadAvatar(
            HttpServletRequest request,
            @RequestParam("file") MultipartFile file
    ) {
        currentUserId(request);
        if (file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "头像文件不能为空");
        }
        if (file.getSize() > 2 * 1024 * 1024) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "头像文件不能超过2MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "只能上传图片文件");
        }
        String originalFilename = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        String extension = extensionOf(originalFilename, contentType);
        try {
            Path uploadDir = Path.of("uploads", "avatars").toAbsolutePath().normalize();
            Files.createDirectories(uploadDir);
            String filename = UUID.randomUUID().toString().replace("-", "") + extension;
            file.transferTo(uploadDir.resolve(filename));
            return Result.success(Map.of("avatarUrl", "/uploads/avatars/" + filename));
        } catch (Exception exception) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "头像上传失败");
        }
    }

    @PutMapping("/password")
    public Result<Void> changePassword(
            HttpServletRequest request,
            @Valid @RequestBody PasswordChangeRequest passwordChangeRequest
    ) {
        String token = extractToken(request);
        Long userId = tokenService.validate(token)
                .map(UserVO::from)
                .map(UserVO::id)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "请先登录"));
        authService.changePassword(userId, passwordChangeRequest);
        tokenService.revoke(token);
        return Result.success(null);
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

    private Long currentUserId(HttpServletRequest request) {
        return tokenService.validate(extractToken(request))
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "请先登录"))
                .id();
    }

    private String extensionOf(String filename, String contentType) {
        String lower = filename.toLowerCase();
        int dotIndex = lower.lastIndexOf('.');
        if (dotIndex >= 0) {
            String extension = lower.substring(dotIndex);
            if (Set.of(".jpg", ".jpeg", ".png", ".gif", ".webp").contains(extension)) {
                return extension;
            }
        }
        return switch (contentType) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/gif" -> ".gif";
            case "image/webp" -> ".webp";
            default -> ".png";
        };
    }
}
