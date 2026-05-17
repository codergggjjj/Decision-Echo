package com.exam.exam_backed.auth.service.impl;

import com.exam.exam_backed.auth.dto.LoginRequest;
import com.exam.exam_backed.auth.service.AuthService;
import com.exam.exam_backed.auth.service.CaptchaService;
import com.exam.exam_backed.auth.service.TokenService;
import com.exam.exam_backed.auth.vo.LoginResponse;
import com.exam.exam_backed.auth.vo.UserVO;
import com.exam.exam_backed.common.BusinessException;
import com.exam.exam_backed.common.ErrorCode;
import com.exam.exam_backed.user.User;
import com.exam.exam_backed.user.mapper.UserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {
    private final UserMapper userMapper;
    private final CaptchaService captchaService;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(
            UserMapper userMapper,
            CaptchaService captchaService,
            TokenService tokenService,
            PasswordEncoder passwordEncoder
    ) {
        this.userMapper = userMapper;
        this.captchaService = captchaService;
        this.tokenService = tokenService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        captchaService.validate(request.captchaId(), request.captchaCode());

        User user = userMapper.findByUsername(request.username())
                .orElseThrow(() -> new BusinessException(ErrorCode.LOGIN_ERROR, "账号或密码错误"));

        if (!passwordEncoder.matches(request.password(), user.passwordHash())) {
            throw new BusinessException(ErrorCode.LOGIN_ERROR, "账号或密码错误");
        }
        if (!user.enabled()) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "账号已禁用");
        }

        return new LoginResponse(tokenService.createToken(user), UserVO.from(user));
    }
}
