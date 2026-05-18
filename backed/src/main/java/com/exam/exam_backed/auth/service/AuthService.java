package com.exam.exam_backed.auth.service;

import com.exam.exam_backed.auth.dto.LoginRequest;
import com.exam.exam_backed.auth.dto.PasswordChangeRequest;
import com.exam.exam_backed.auth.dto.RegisterRequest;
import com.exam.exam_backed.auth.vo.LoginResponse;

public interface AuthService {
    LoginResponse login(LoginRequest request);

    LoginResponse register(RegisterRequest request);

    void changePassword(Long userId, PasswordChangeRequest request);
}
