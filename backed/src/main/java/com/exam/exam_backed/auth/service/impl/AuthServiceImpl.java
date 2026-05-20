package com.exam.exam_backed.auth.service.impl;

import com.exam.exam_backed.auth.dto.LoginRequest;
import com.exam.exam_backed.auth.dto.PasswordChangeRequest;
import com.exam.exam_backed.auth.dto.ProfileUpdateRequest;
import com.exam.exam_backed.auth.dto.RegisterRequest;
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
import org.springframework.transaction.annotation.Transactional;

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

    @Override
    @Transactional
    public LoginResponse register(RegisterRequest request) {
        captchaService.validate(request.captchaId(), request.captchaCode());
        if (!request.password().equals(request.confirmPassword())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "两次输入的密码不一致");
        }
        if (userMapper.findByUsername(request.username()).isPresent()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "用户名已存在");
        }

        User user = new User(
                null,
                request.username(),
                passwordEncoder.encode(request.password()),
                request.nickname(),
                null,
                1
        );
        userMapper.insert(user);
        User saved = userMapper.findByUsername(request.username())
                .orElseThrow(() -> new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败"));
        return new LoginResponse(tokenService.createToken(saved), UserVO.from(saved));
    }

    @Override
    @Transactional
    public void changePassword(Long userId, PasswordChangeRequest request) {
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "两次输入的密码不一致");
        }
        if (request.oldPassword().equals(request.newPassword())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "新密码不能和旧密码相同");
        }
        User user = userMapper.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "请先登录"));
        if (!passwordEncoder.matches(request.oldPassword(), user.passwordHash())) {
            throw new BusinessException(ErrorCode.LOGIN_ERROR, "旧密码不正确");
        }
        userMapper.updatePassword(userId, passwordEncoder.encode(request.newPassword()));
    }

    @Override
    @Transactional
    public UserVO updateProfile(Long userId, ProfileUpdateRequest request) {
        userMapper.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "请先登录"));
        String avatarUrl = request.avatarUrl() == null || request.avatarUrl().isBlank()
                ? null
                : request.avatarUrl().trim();
        userMapper.updateProfile(userId, request.nickname().trim(), avatarUrl);
        return userMapper.findById(userId)
                .map(UserVO::from)
                .orElseThrow(() -> new BusinessException(ErrorCode.SYSTEM_ERROR, "资料更新失败"));
    }

    @Override
    public UserVO currentUser(Long userId) {
        return userMapper.findById(userId)
                .map(UserVO::from)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "请先登录"));
    }
}
