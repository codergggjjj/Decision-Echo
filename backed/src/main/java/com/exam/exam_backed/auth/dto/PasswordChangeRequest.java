package com.exam.exam_backed.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PasswordChangeRequest(
        @NotBlank(message = "旧密码不能为空")
        String oldPassword,

        @NotBlank(message = "新密码不能为空")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,32}$", message = "新密码需要8-32位且至少包含字母和数字")
        String newPassword,

        @NotBlank(message = "确认密码不能为空")
        String confirmPassword
) {
}
