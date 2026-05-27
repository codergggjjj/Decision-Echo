package com.exam.exam_backed.admin;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.exam.exam_backed.admin.mapper.AdminMapper;
import com.exam.exam_backed.admin.service.AdminService;
import com.exam.exam_backed.admin.service.impl.AdminServiceImpl;
import com.exam.exam_backed.admin.vo.AdminDecisionVO;
import com.exam.exam_backed.admin.vo.AdminStatsVO;
import com.exam.exam_backed.admin.vo.AdminUserVO;
import com.exam.exam_backed.common.BusinessException;
import com.exam.exam_backed.decision.Decision;
import com.exam.exam_backed.decision.mapper.DecisionMapper;
import com.exam.exam_backed.support.AbstractBaseMapperStub;
import com.exam.exam_backed.user.User;
import com.exam.exam_backed.user.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdminServiceImplTest {
    private final FakeAdminMapper adminMapper = new FakeAdminMapper();
    private final FakeUserMapper userMapper = new FakeUserMapper();
    private final FakeDecisionMapper decisionMapper = new FakeDecisionMapper();
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final AdminService adminService = new AdminServiceImpl(adminMapper, userMapper, decisionMapper, passwordEncoder);

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
        assertTrue(userMapper.lastSqlSegment.contains("LIMIT 100"));
        assertEquals(List.of("%admin%", "%admin%"), userMapper.lastQueryParams);
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

    @Test
    void resetPasswordSetsUserPasswordToInitialPassword() {
        adminService.resetPassword(7L);

        assertEquals(7L, userMapper.updatedUserId);
        assertTrue(passwordEncoder.matches("123456", userMapper.updatedPasswordHash));
    }

    private static class FakeAdminMapper implements AdminMapper {
        private int lastDecisionLimit;
        private String lastDecisionKeyword;
        private String lastDecisionStatus;

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
                            "study",
                            "calm",
                            2,
                            LocalDateTime.now().plusDays(7),
                            "satisfied",
                            "reviewed",
                            LocalDateTime.now()
                    )
            );
        }
    }

    private static class FakeUserMapper extends AbstractBaseMapperStub<User> implements UserMapper {
        private String lastSqlSegment;
        private List<Object> lastQueryParams = List.of();
        private Long updatedUserId;
        private String updatedPasswordHash;

        @Override
        public Long selectCount(Wrapper<User> queryWrapper) {
            return 3L;
        }

        @Override
        public List<User> selectList(Wrapper<User> queryWrapper) {
            lastSqlSegment = queryWrapper.getSqlSegment();
            if (queryWrapper instanceof QueryWrapper<User> query) {
                lastQueryParams = new ArrayList<>(query.getParamNameValuePairs().values());
            }
            return List.of(
                    new User(2L, "admin", "hash", "admin", null, 1, "admin", LocalDateTime.now())
            );
        }

        @Override
        public int updatePassword(Long id, String passwordHash) {
            updatedUserId = id;
            updatedPasswordHash = passwordHash;
            return 1;
        }
    }

    private static class FakeDecisionMapper extends AbstractBaseMapperStub<Decision> implements DecisionMapper {
        private int countCall;

        @Override
        public Long selectCount(Wrapper<Decision> queryWrapper) {
            countCall++;
            return switch (countCall) {
                case 1 -> 5L;
                case 2 -> 2L;
                case 3 -> 3L;
                default -> 0L;
            };
        }

        @Override
        public int countReviewedBySatisfactionAndFilters(Long userId, String satisfaction, String tag, String mood) {
            throw unsupported();
        }

        @Override
        public List<MoodSatisfactionCount> countReviewedByMoodAndSatisfaction(Long userId) {
            throw unsupported();
        }

        @Override
        public List<TrendCount> countCreatedByMonth(Long userId, LocalDateTime start, LocalDateTime end) {
            throw unsupported();
        }

        @Override
        public List<TrendCount> countCreatedByDay(Long userId, LocalDateTime start, LocalDateTime end) {
            throw unsupported();
        }

        @Override
        public List<String> findTagsByUserId(Long userId) {
            throw unsupported();
        }
    }
}
