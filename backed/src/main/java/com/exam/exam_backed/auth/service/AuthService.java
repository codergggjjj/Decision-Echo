package com.exam.exam_backed.auth.service;

import com.exam.exam_backed.auth.dto.LoginRequest;
import com.exam.exam_backed.auth.vo.LoginResponse;

public interface AuthService {
    LoginResponse login(LoginRequest request);
}
