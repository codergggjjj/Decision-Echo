package com.exam.exam_backed.auth.vo;

public record LoginResponse(String token, UserVO user) {
}
