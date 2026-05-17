package com.exam.exam_backed.auth;

import com.exam.exam_backed.auth.dto.LoginRequest;
import com.exam.exam_backed.auth.service.AuthService;
import com.exam.exam_backed.auth.service.CaptchaService;
import com.exam.exam_backed.auth.service.TokenService;
import com.exam.exam_backed.auth.service.impl.AuthServiceImpl;
import com.exam.exam_backed.auth.service.impl.InMemoryCaptchaService;
import com.exam.exam_backed.auth.service.impl.InMemoryTokenService;
import com.exam.exam_backed.common.BusinessException;
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
    private final TokenService tokenService = new InMemoryTokenService();
    private final FakeUserMapper userMapper = new FakeUserMapper();
    private final AuthService authService = new AuthServiceImpl(
            userMapper,
            captchaService,
            tokenService,
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
        assertTrue(tokenService.validate(response.token()).isPresent());
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

    private static class FakeUserMapper implements UserMapper {
        private final Map<String, User> users = new ConcurrentHashMap<>();

        void save(User user) {
            users.put(user.username(), user);
        }

        @Override
        public Optional<User> findByUsername(String username) {
            return Optional.ofNullable(users.get(username));
        }
    }
}
