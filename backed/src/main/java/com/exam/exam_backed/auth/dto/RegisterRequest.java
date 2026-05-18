package com.exam.exam_backed.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "用户名不能为空")
        @Pattern(regexp = "^[A-Za-z0-9_]{4,50}$", message = "用户名只能包含4-50位字母、数字或下划线")
        String username,

        @NotBlank(message = "昵称不能为空")
        @Size(min = 2, max = 20, message = "昵称长度需要在2-20位之间")
        String nickname,

        @NotBlank(message = "密码不能为空")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,32}$", message = "密码需要8-32位且至少包含字母和数字")
        String password,

        @NotBlank(message = "确认密码不能为空")
        String confirmPassword,

        @NotBlank(message = "验证码标识不能为空")
        String captchaId,

        @NotBlank(message = "验证码不能为空")
        String captchaCode
) {
}
