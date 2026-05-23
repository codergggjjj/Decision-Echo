package com.exam.exam_backed.admin.service.impl;

import com.exam.exam_backed.admin.mapper.AdminMapper;
import com.exam.exam_backed.admin.service.AdminService;
import com.exam.exam_backed.admin.vo.AdminDecisionVO;
import com.exam.exam_backed.admin.vo.AdminStatsVO;
import com.exam.exam_backed.admin.vo.AdminUserVO;
import com.exam.exam_backed.common.BusinessException;
import com.exam.exam_backed.common.ErrorCode;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class AdminServiceImpl implements AdminService {
    private static final int DEFAULT_LIMIT = 50;
    private static final int MAX_LIMIT = 100;
    private static final Set<String> ALLOWED_STATUS = Set.of("pending", "reviewed");

    private final AdminMapper adminMapper;

    public AdminServiceImpl(AdminMapper adminMapper) {
        this.adminMapper = adminMapper;
    }

    @Override
    public AdminStatsVO stats() {
        return new AdminStatsVO(
                adminMapper.countUsers(),
                adminMapper.countDecisions(),
                adminMapper.countDecisionsByStatus("reviewed"),
                adminMapper.countDecisionsByStatus("pending")
        );
    }

    @Override
    public List<AdminUserVO> users(String keyword, int limit) {
        return adminMapper.findUsers(normalizeText(keyword), normalizeLimit(limit));
    }

    @Override
    public List<AdminDecisionVO> decisions(String keyword, String status, int limit) {
        String normalizedStatus = normalizeStatus(status);
        return adminMapper.findDecisions(normalizeText(keyword), normalizedStatus, normalizeLimit(limit));
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
}
