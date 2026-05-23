package com.exam.exam_backed.admin;

import com.exam.exam_backed.admin.mapper.AdminMapper;
import com.exam.exam_backed.admin.service.AdminService;
import com.exam.exam_backed.admin.service.impl.AdminServiceImpl;
import com.exam.exam_backed.admin.vo.AdminDecisionVO;
import com.exam.exam_backed.admin.vo.AdminStatsVO;
import com.exam.exam_backed.admin.vo.AdminUserVO;
import com.exam.exam_backed.common.BusinessException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AdminServiceImplTest {
    private final FakeAdminMapper adminMapper = new FakeAdminMapper();
    private final AdminService adminService = new AdminServiceImpl(adminMapper);

    @Test
    void statsReturnsSiteWideCountsWithoutDeletedDecisions() {
        AdminStatsVO stats = adminService.stats();

        assertEquals(3, stats.userTotal());
        assertEquals(5, stats.decisionTotal());
        assertEquals(2, stats.reviewedTotal());
        assertEquals(3, stats.pendingTotal());
    }

    @Test
    void usersTrimsKeywordAndCapsLimit() {
        List<AdminUserVO> users = adminService.users(" admin ", 200);

        assertEquals(List.of("admin"), users.stream().map(AdminUserVO::username).toList());
        assertEquals(100, adminMapper.lastUserLimit);
        assertEquals("admin", adminMapper.lastUserKeyword);
    }

    @Test
    void decisionsRejectsInvalidStatus() {
        BusinessException error = assertThrows(BusinessException.class, () ->
                adminService.decisions(null, "deleted", 50)
        );

        assertEquals("决策状态筛选值不正确", error.getMessage());
    }

    @Test
    void decisionsAcceptsReviewedStatusAndNormalizesLimit() {
        List<AdminDecisionVO> decisions = adminService.decisions(" course ", "reviewed", 0);

        assertEquals(List.of("Weekend Course"), decisions.stream().map(AdminDecisionVO::title).toList());
        assertEquals(50, adminMapper.lastDecisionLimit);
        assertEquals("course", adminMapper.lastDecisionKeyword);
        assertEquals("reviewed", adminMapper.lastDecisionStatus);
    }

    private static class FakeAdminMapper implements AdminMapper {
        private int lastUserLimit;
        private String lastUserKeyword;
        private int lastDecisionLimit;
        private String lastDecisionKeyword;
        private String lastDecisionStatus;

        @Override
        public int countUsers() {
            return 3;
        }

        @Override
        public int countDecisions() {
            return 5;
        }

        @Override
        public int countDecisionsByStatus(String status) {
            return switch (status) {
                case "reviewed" -> 2;
                case "pending" -> 3;
                default -> 0;
            };
        }

        @Override
        public List<AdminUserVO> findUsers(String keyword, int limit) {
            lastUserKeyword = keyword;
            lastUserLimit = limit;
            return List.of(
                    new AdminUserVO(2L, "admin", "管理员", null, 1, "admin", LocalDateTime.now())
            );
        }

        @Override
        public List<AdminDecisionVO> findDecisions(String keyword, String status, int limit) {
            lastDecisionKeyword = keyword;
            lastDecisionStatus = status;
            lastDecisionLimit = limit;
            return List.of(
                    new AdminDecisionVO(
                            1L,
                            1L,
                            "test_user",
                            "Weekend Course",
                            "学习",
                            "平静",
                            2,
                            LocalDateTime.now().plusDays(7),
                            "满意",
                            "reviewed",
                            LocalDateTime.now()
                    )
            );
        }
    }
}
