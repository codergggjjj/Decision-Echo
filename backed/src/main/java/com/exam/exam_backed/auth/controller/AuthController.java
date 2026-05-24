package com.exam.exam_backed.auth.controller;

import com.exam.exam_backed.auth.dto.LoginRequest;
import com.exam.exam_backed.auth.dto.PasswordChangeRequest;
import com.exam.exam_backed.auth.dto.ProfileUpdateRequest;
import com.exam.exam_backed.auth.dto.RegisterRequest;
import com.exam.exam_backed.auth.service.AuthSessionService;
import com.exam.exam_backed.auth.service.AuthService;
import com.exam.exam_backed.auth.service.CaptchaService;
import com.exam.exam_backed.auth.vo.CaptchaResponse;
import com.exam.exam_backed.auth.vo.LoginResponse;
import com.exam.exam_backed.auth.vo.UserVO;
import com.exam.exam_backed.common.BusinessException;
import com.exam.exam_backed.common.ErrorCode;
import com.exam.exam_backed.common.Result;
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
    private final AuthSessionService authSessionService;

    public AuthController(AuthService authService, CaptchaService captchaService, AuthSessionService authSessionService) {
        this.authService = authService;
        this.captchaService = captchaService;
        this.authSessionService = authSessionService;
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
    public Result<UserVO> me() {
        return Result.success(authService.currentUser(currentUserId()));
    }

    @PutMapping("/profile")
    public Result<UserVO> updateProfile(@Valid @RequestBody ProfileUpdateRequest body) {
        return Result.success(authService.updateProfile(currentUserId(), body));
    }

    @PostMapping("/avatar")
    public Result<Map<String, String>> uploadAvatar(
            @RequestParam("file") MultipartFile file
    ) {
        currentUserId();
        if (file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "澶村儚鏂囦欢涓嶈兘涓虹┖");
        }
        if (file.getSize() > 2 * 1024 * 1024) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "澶村儚鏂囦欢涓嶈兘瓒呰繃2MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "鍙兘涓婁紶鍥剧墖鏂囦欢");
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
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "澶村儚涓婁紶澶辫触");
        }
    }

    @PutMapping("/password")
    public Result<Void> changePassword(
            @Valid @RequestBody PasswordChangeRequest passwordChangeRequest
    ) {
        Long userId = currentUserId();
        authService.changePassword(userId, passwordChangeRequest);
        authSessionService.logout();
        return Result.success(null);
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        authSessionService.logout();
        return Result.success(null);
    }

    private Long currentUserId() {
        return authSessionService.currentUserId();
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
