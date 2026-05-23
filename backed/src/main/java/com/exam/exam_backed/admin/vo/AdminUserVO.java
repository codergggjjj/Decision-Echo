package com.exam.exam_backed.admin.vo;

import java.time.LocalDateTime;

public record AdminUserVO(
        Long id,
        String username,
        String nickname,
        String avatarUrl,
        Integer status,
        String role,
        LocalDateTime createTime
) {
}
