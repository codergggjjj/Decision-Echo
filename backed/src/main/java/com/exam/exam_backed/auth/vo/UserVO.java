package com.exam.exam_backed.auth.vo;

import com.exam.exam_backed.user.User;

import java.time.LocalDateTime;

public record UserVO(Long id, String username, String nickname, String avatarUrl, Integer status, String role, LocalDateTime createTime) {
    public static UserVO from(User user) {
        return new UserVO(user.id(), user.username(), user.nickname(), user.avatarUrl(), user.status(), user.role(), user.createTime());
    }
}
