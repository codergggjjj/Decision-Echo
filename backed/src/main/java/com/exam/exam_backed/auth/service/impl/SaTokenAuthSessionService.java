package com.exam.exam_backed.auth.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.exam.exam_backed.auth.service.AuthSessionService;
import com.exam.exam_backed.user.User;
import org.springframework.stereotype.Service;

@Service
public class SaTokenAuthSessionService implements AuthSessionService {
    @Override
    public String createToken(User user) {
        StpUtil.login(user.id());
        return StpUtil.getTokenValue();
    }

    @Override
    public Long currentUserId() {
        return StpUtil.getLoginIdAsLong();
    }

    @Override
    public void checkLogin() {
        StpUtil.checkLogin();
    }

    @Override
    public void logout() {
        StpUtil.logout();
    }
}
