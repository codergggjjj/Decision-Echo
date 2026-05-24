package com.exam.exam_backed.auth.service;

import com.exam.exam_backed.user.User;

public interface AuthSessionService {
    String createToken(User user);

    Long currentUserId();

    void checkLogin();

    void logout();
}
