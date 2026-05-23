package com.exam.exam_backed.admin.service;

import com.exam.exam_backed.admin.vo.AdminDecisionVO;
import com.exam.exam_backed.admin.vo.AdminStatsVO;
import com.exam.exam_backed.admin.vo.AdminUserVO;

import java.util.List;

public interface AdminService {
    AdminStatsVO stats();

    List<AdminUserVO> users(String keyword, int limit);

    List<AdminDecisionVO> decisions(String keyword, String status, int limit);
}
