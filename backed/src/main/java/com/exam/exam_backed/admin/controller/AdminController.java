package com.exam.exam_backed.admin.controller;

import com.exam.exam_backed.admin.service.AdminService;
import com.exam.exam_backed.admin.vo.AdminDecisionVO;
import com.exam.exam_backed.admin.vo.AdminStatsVO;
import com.exam.exam_backed.admin.vo.AdminUserVO;
import com.exam.exam_backed.auth.service.TokenService;
import com.exam.exam_backed.common.BusinessException;
import com.exam.exam_backed.common.ErrorCode;
import com.exam.exam_backed.common.Result;
import com.exam.exam_backed.user.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final AdminService adminService;
    private final TokenService tokenService;

    public AdminController(AdminService adminService, TokenService tokenService) {
        this.adminService = adminService;
        this.tokenService = tokenService;
    }

    @GetMapping("/stats")
    public Result<AdminStatsVO> stats(HttpServletRequest request) {
        requireAdmin(request);
        return Result.success(adminService.stats());
    }

    @GetMapping("/users")
    public Result<List<AdminUserVO>> users(
            HttpServletRequest request,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "50") int limit
    ) {
        requireAdmin(request);
        return Result.success(adminService.users(keyword, limit));
    }

    @GetMapping("/decisions")
    public Result<List<AdminDecisionVO>> decisions(
            HttpServletRequest request,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "50") int limit
    ) {
        requireAdmin(request);
        return Result.success(adminService.decisions(keyword, status, limit));
    }

    private void requireAdmin(HttpServletRequest request) {
        User user = tokenService.validate(extractToken(request))
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "请先登录"));
        if (!user.admin()) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权访问后台管理");
        }
    }

    private String extractToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        return authorization;
    }
}
