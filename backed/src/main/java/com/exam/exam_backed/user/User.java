package com.exam.exam_backed.user;

import java.time.LocalDateTime;

public record User(
        Long id,
        String username,
        String passwordHash,
        String nickname,
        String avatarUrl,
        Integer status,
        String role,
        LocalDateTime createTime
) {
    public User(Long id, String username, String passwordHash, String nickname, Integer status) {
        this(id, username, passwordHash, nickname, null, status, null);
    }

    public User(Long id, String username, String passwordHash, String nickname, String avatarUrl, Integer status) {
        this(id, username, passwordHash, nickname, avatarUrl, status, null);
    }

    public User(Long id, String username, String passwordHash, String nickname, String avatarUrl, Integer status, LocalDateTime createTime) {
        this(id, username, passwordHash, nickname, avatarUrl, status, "user", createTime);
    }

    public boolean enabled() {
        return Integer.valueOf(1).equals(status);
    }

    public boolean admin() {
        return "admin".equals(role);
    }
}
