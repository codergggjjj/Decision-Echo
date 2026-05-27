package com.exam.exam_backed.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.exam.exam_backed.admin.mapper.AdminMapper;
import com.exam.exam_backed.admin.service.AdminService;
import com.exam.exam_backed.admin.vo.AdminDecisionVO;
import com.exam.exam_backed.admin.vo.AdminStatsVO;
import com.exam.exam_backed.admin.vo.AdminUserVO;
import com.exam.exam_backed.common.BusinessException;
import com.exam.exam_backed.common.ErrorCode;
import com.exam.exam_backed.decision.Decision;
import com.exam.exam_backed.decision.mapper.DecisionMapper;
import com.exam.exam_backed.user.User;
import com.exam.exam_backed.user.mapper.UserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class AdminServiceImpl implements AdminService {
    private static final int DEFAULT_LIMIT = 50;
    private static final int MAX_LIMIT = 100;
    private static final Set<String> ALLOWED_STATUS = Set.of("pending", "reviewed");

    private final AdminMapper adminMapper;
    private final UserMapper userMapper;
    private final DecisionMapper decisionMapper;
    private final PasswordEncoder passwordEncoder;

    public AdminServiceImpl(
            AdminMapper adminMapper,
            UserMapper userMapper,
            DecisionMapper decisionMapper,
            PasswordEncoder passwordEncoder
    ) {
        this.adminMapper = adminMapper;
        this.userMapper = userMapper;
        this.decisionMapper = decisionMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public AdminStatsVO stats() {
        return new AdminStatsVO(
                userMapper.selectCount(null).intValue(),
                countDecisions(null),
                countDecisions("reviewed"),
                countDecisions("pending")
        );
    }

    @Override
    public List<AdminUserVO> users(String keyword, int limit) {
        String normalizedKeyword = normalizeText(keyword);
        QueryWrapper<User> query = new QueryWrapper<User>()
                .orderByDesc("create_time", "id")
                .last("LIMIT " + normalizeLimit(limit));
        if (normalizedKeyword != null) {
            query.and(wrapper -> wrapper
                    .like("username", normalizedKeyword)
                    .or()
                    .like("nickname", normalizedKeyword));
        }
        return userMapper.selectList(query)
                .stream()
                .map(user -> new AdminUserVO(
                        user.id(),
                        user.username(),
                        user.nickname(),
                        user.avatarUrl(),
                        user.status(),
                        user.role(),
                        user.createTime()
                ))
                .toList();
    }

    @Override
    public List<AdminDecisionVO> decisions(String keyword, String status, int limit) {
        String normalizedStatus = normalizeStatus(status);
        return adminMapper.findDecisions(normalizeText(keyword), normalizedStatus, normalizeLimit(limit));
    }

    @Override
    public void resetPassword(Long userId) {
        int updated = userMapper.updatePassword(userId, passwordEncoder.encode("123456"));
        if (updated <= 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "用户不存在");
        }
    }

    private String normalizeText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private String normalizeStatus(String status) {
        String value = normalizeText(status);
        if (value == null) {
            return null;
        }
        if (!ALLOWED_STATUS.contains(value)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "决策状态筛选值不正确");
        }
        return value;
    }

    private int normalizeLimit(int limit) {
        if (limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }

    private int countDecisions(String status) {
        QueryWrapper<Decision> query = new QueryWrapper<Decision>().eq("deleted", 0);
        if (status != null) {
            query.eq("status", status);
        }
        return decisionMapper.selectCount(query).intValue();
    }
}
