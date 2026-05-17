package com.exam.exam_backed.auth.vo;

import com.exam.exam_backed.user.User;

public record UserVO(Long id, String username, String nickname) {
    public static UserVO from(User user) {
        return new UserVO(user.id(), user.username(), user.nickname());
    }
}
