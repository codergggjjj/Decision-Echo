package com.exam.exam_backed.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProfileUpdateRequest(
        @NotBlank(message = "昵称不能为空")
        @Size(max = 50, message = "昵称不能超过50个字符")
        String nickname,

        @Size(max = 500, message = "头像地址不能超过500个字符")
        String avatarUrl
) {
}
