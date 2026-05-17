package com.exam.exam_backed.user;

public record User(Long id, String username, String passwordHash, String nickname, Integer status) {
    public boolean enabled() {
        return Integer.valueOf(1).equals(status);
    }
}
