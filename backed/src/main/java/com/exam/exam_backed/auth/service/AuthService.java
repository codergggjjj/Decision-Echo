package com.exam.exam_backed.auth.service;

import com.exam.exam_backed.auth.dto.LoginRequest;
import com.exam.exam_backed.auth.dto.PasswordChangeRequest;
import com.exam.exam_backed.auth.dto.ProfileUpdateRequest;
import com.exam.exam_backed.auth.dto.RegisterRequest;
import com.exam.exam_backed.auth.vo.LoginResponse;
import com.exam.exam_backed.auth.vo.UserVO;

public interface AuthService {
    LoginResponse login(LoginRequest request);

    LoginResponse register(RegisterRequest request);

    void changePassword(Long userId, PasswordChangeRequest request);

    UserVO updateProfile(Long userId, ProfileUpdateRequest request);

    UserVO currentUser(Long userId);
}
