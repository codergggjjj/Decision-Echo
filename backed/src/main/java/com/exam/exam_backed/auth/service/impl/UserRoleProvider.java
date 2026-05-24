package com.exam.exam_backed.auth.service.impl;

import cn.dev33.satoken.stp.StpInterface;
import com.exam.exam_backed.user.mapper.UserMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserRoleProvider implements StpInterface {
    private final UserMapper userMapper;

    public UserRoleProvider(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return List.of();
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        Long userId = Long.valueOf(loginId.toString());
        return userMapper.findById(userId)
                .map(user -> user.role() == null ? List.<String>of() : List.of(user.role()))
                .orElse(List.of());
    }
}
