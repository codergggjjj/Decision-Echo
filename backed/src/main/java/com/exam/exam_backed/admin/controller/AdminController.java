package com.exam.exam_backed.admin.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.exam.exam_backed.admin.service.AdminService;
import com.exam.exam_backed.admin.vo.AdminDecisionVO;
import com.exam.exam_backed.admin.vo.AdminStatsVO;
import com.exam.exam_backed.admin.vo.AdminUserVO;
import com.exam.exam_backed.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/stats")
    public Result<AdminStatsVO> stats() {
        requireAdmin();
        return Result.success(adminService.stats());
    }

    @GetMapping("/users")
    public Result<List<AdminUserVO>> users(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "50") int limit
    ) {
        requireAdmin();
        return Result.success(adminService.users(keyword, limit));
    }

    @GetMapping("/decisions")
    public Result<List<AdminDecisionVO>> decisions(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "50") int limit
    ) {
        requireAdmin();
        return Result.success(adminService.decisions(keyword, status, limit));
    }

    @PutMapping("/users/{id}/password/reset")
    public Result<Void> resetPassword(@PathVariable Long id) {
        requireAdmin();
        adminService.resetPassword(id);
        return Result.success(null);
    }

    private void requireAdmin() {
        StpUtil.checkRole("admin");
    }
}
