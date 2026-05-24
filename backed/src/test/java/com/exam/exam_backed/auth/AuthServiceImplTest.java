package com.exam.exam_backed.auth;

import com.exam.exam_backed.auth.dto.LoginRequest;
import com.exam.exam_backed.auth.dto.PasswordChangeRequest;
import com.exam.exam_backed.auth.dto.ProfileUpdateRequest;
import com.exam.exam_backed.auth.dto.RegisterRequest;
import com.exam.exam_backed.auth.service.AuthSessionService;
import com.exam.exam_backed.auth.service.AuthService;
import com.exam.exam_backed.auth.service.CaptchaService;
import com.exam.exam_backed.auth.service.impl.AuthServiceImpl;
import com.exam.exam_backed.auth.service.impl.InMemoryCaptchaService;
import com.exam.exam_backed.common.BusinessException;
import com.exam.exam_backed.support.AbstractBaseMapperStub;
import com.exam.exam_backed.user.User;
import com.exam.exam_backed.user.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

class AuthServiceImplTest {

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final CaptchaService captchaService = new InMemoryCaptchaService();
    private final FakeAuthSessionService authSessionService = new FakeAuthSessionService();
    private final FakeUserMapper userMapper = new FakeUserMapper();
    private final AuthService authService = new AuthServiceImpl(
            userMapper,
            captchaService,
            authSessionService,
            passwordEncoder
    );

    @Test
    void loginReturnsTokenWhenCredentialsAndCaptchaAreValid() {
        userMapper.save(new User(1L, "test_user", passwordEncoder.encode("Test@123456"), "测试用户", 1));
        var captcha = captchaService.createCaptcha();

        var response = authService.login(new LoginRequest(
                "test_user",
                "Test@123456",
                captcha.captchaId(),
                captcha.answerForTest()
        ));

        assertNotNull(response.token());
        assertEquals("test_user", response.user().username());
        assertTrue(authSessionService.valid(response.token()));
    }

    @Test
    void loginRejectsInvalidCaptchaBeforeCheckingPassword() {
        userMapper.save(new User(1L, "test_user", passwordEncoder.encode("Test@123456"), "测试用户", 1));
        var captcha = captchaService.createCaptcha();

        var error = assertThrows(BusinessException.class, () -> authService.login(new LoginRequest(
                "test_user",
                "wrong-password",
                captcha.captchaId(),
                "0000"
        )));

        assertEquals("验证码错误", error.getMessage());
    }

    @Test
    void loginRejectsReusedCaptcha() {
        userMapper.save(new User(1L, "test_user", passwordEncoder.encode("Test@123456"), "测试用户", 1));
        var captcha = captchaService.createCaptcha();
        var request = new LoginRequest("test_user", "Test@123456", captcha.captchaId(), captcha.answerForTest());

        authService.login(request);
        var error = assertThrows(BusinessException.class, () -> authService.login(request));

        assertEquals("验证码不存在或已失效", error.getMessage());
    }

    @Test
    void loginRejectsDisabledUser() {
        userMapper.save(new User(2L, "disabled_user", passwordEncoder.encode("Disabled@123456"), "禁用用户", 0));
        var captcha = captchaService.createCaptcha();

        var error = assertThrows(BusinessException.class, () -> authService.login(new LoginRequest(
                "disabled_user",
                "Disabled@123456",
                captcha.captchaId(),
                captcha.answerForTest()
        )));

        assertEquals("账号已禁用", error.getMessage());
    }

    @Test
    void registerCreatesEnabledUserWithEncodedPasswordAndToken() {
        var captcha = captchaService.createCaptcha();

        var response = authService.register(new RegisterRequest(
                "new_user",
                "新朋友",
                "NewUser123",
                "NewUser123",
                captcha.captchaId(),
                captcha.answerForTest()
        ));

        User saved = userMapper.findByUsername("new_user").orElseThrow();
        assertNotNull(saved.id());
        assertEquals("新朋友", saved.nickname());
        assertEquals(1, saved.status());
        assertTrue(passwordEncoder.matches("NewUser123", saved.passwordHash()));
        assertEquals("new_user", response.user().username());
        assertTrue(authSessionService.valid(response.token()));
    }

    @Test
    void registerRejectsDuplicateUsername() {
        userMapper.save(new User(1L, "new_user", passwordEncoder.encode("OldPass123"), "旧用户", 1));
        var captcha = captchaService.createCaptcha();

        var error = assertThrows(BusinessException.class, () -> authService.register(new RegisterRequest(
                "new_user",
                "新朋友",
                "NewUser123",
                "NewUser123",
                captcha.captchaId(),
                captcha.answerForTest()
        )));

        assertEquals("用户名已存在", error.getMessage());
    }

    @Test
    void registerRejectsInvalidCaptcha() {
        var captcha = captchaService.createCaptcha();

        var error = assertThrows(BusinessException.class, () -> authService.register(new RegisterRequest(
                "new_user",
                "新朋友",
                "NewUser123",
                "NewUser123",
                captcha.captchaId(),
                "0000"
        )));

        assertEquals("验证码错误", error.getMessage());
    }

    @Test
    void registerRejectsMismatchedConfirmPassword() {
        var captcha = captchaService.createCaptcha();

        var error = assertThrows(BusinessException.class, () -> authService.register(new RegisterRequest(
                "new_user",
                "新朋友",
                "NewUser123",
                "OtherPass123",
                captcha.captchaId(),
                captcha.answerForTest()
        )));

        assertEquals("两次输入的密码不一致", error.getMessage());
    }

    @Test
    void changePasswordUpdatesPasswordWhenOldPasswordIsCorrect() {
        userMapper.save(new User(1L, "test_user", passwordEncoder.encode("OldPass123"), "测试用户", 1));

        authService.changePassword(1L, new PasswordChangeRequest("OldPass123", "NewPass123", "NewPass123"));

        User saved = userMapper.findByUsername("test_user").orElseThrow();
        assertFalse(passwordEncoder.matches("OldPass123", saved.passwordHash()));
        assertTrue(passwordEncoder.matches("NewPass123", saved.passwordHash()));
    }

    @Test
    void changePasswordRejectsWrongOldPassword() {
        userMapper.save(new User(1L, "test_user", passwordEncoder.encode("OldPass123"), "测试用户", 1));

        var error = assertThrows(BusinessException.class, () ->
                authService.changePassword(1L, new PasswordChangeRequest("WrongPass123", "NewPass123", "NewPass123"))
        );

        assertEquals("旧密码不正确", error.getMessage());
    }

    @Test
    void updateProfileChangesNicknameAndAvatar() {
        userMapper.save(new User(1L, "test_user", passwordEncoder.encode("OldPass123"), "测试用户", 1));

        var updated = authService.updateProfile(1L, new ProfileUpdateRequest("新的昵称", "/uploads/avatars/a.png"));

        assertEquals("test_user", updated.username());
        assertEquals("新的昵称", updated.nickname());
        assertEquals("/uploads/avatars/a.png", updated.avatarUrl());
        User saved = userMapper.findById(1L).orElseThrow();
        assertEquals("新的昵称", saved.nickname());
        assertEquals("/uploads/avatars/a.png", saved.avatarUrl());
    }

    @Test
    void currentUserReturnsLatestProfileFromDatabase() {
        userMapper.save(new User(1L, "test_user", passwordEncoder.encode("OldPass123"), "测试用户", 1));
        authService.updateProfile(1L, new ProfileUpdateRequest("新的昵称", "/uploads/avatars/a.png"));

        var currentUser = authService.currentUser(1L);

        assertEquals("新的昵称", currentUser.nickname());
        assertEquals("/uploads/avatars/a.png", currentUser.avatarUrl());
    }

    private static class FakeAuthSessionService implements AuthSessionService {
        private final Map<String, Long> sessions = new ConcurrentHashMap<>();
        private long tokenSequence = 1L;
        private Long currentUserId;

        @Override
        public String createToken(User user) {
            String token = "token-" + tokenSequence++;
            sessions.put(token, user.id());
            currentUserId = user.id();
            return token;
        }

        @Override
        public Long currentUserId() {
            return currentUserId;
        }

        @Override
        public void checkLogin() {
            if (currentUserId == null) {
                throw new IllegalStateException("not login");
            }
        }

        @Override
        public void logout() {
            currentUserId = null;
        }

        boolean valid(String token) {
            return sessions.containsKey(token);
        }
    }

    private static class FakeUserMapper extends AbstractBaseMapperStub<User> implements UserMapper {
        private final Map<String, User> users = new ConcurrentHashMap<>();
        private long nextId = 1L;

        void save(User user) {
            users.put(user.username(), user);
            nextId = Math.max(nextId, user.id() == null ? nextId : user.id() + 1);
        }

        @Override
        public Optional<User> findByUsername(String username) {
            return Optional.ofNullable(users.get(username));
        }

        @Override
        public Optional<User> findById(Long id) {
            return users.values().stream()
                    .filter(user -> user.id().equals(id))
                    .findFirst();
        }

        @Override
        public int insert(User user) {
            save(new User(nextId++, user.username(), user.passwordHash(), user.nickname(), user.avatarUrl(), user.status()));
            return 1;
        }

        @Override
        public int updatePassword(Long id, String passwordHash) {
            User user = findById(id).orElse(null);
            if (user == null) {
                return 0;
            }
            users.put(user.username(), new User(user.id(), user.username(), passwordHash, user.nickname(), user.avatarUrl(), user.status(), user.createTime()));
            return 1;
        }

        @Override
        public int updateProfile(Long id, String nickname, String avatarUrl) {
            User user = findById(id).orElse(null);
            if (user == null) {
                return 0;
            }
            users.put(user.username(), new User(user.id(), user.username(), user.passwordHash(), nickname, avatarUrl, user.status(), user.createTime()));
            return 1;
        }
    }
}
