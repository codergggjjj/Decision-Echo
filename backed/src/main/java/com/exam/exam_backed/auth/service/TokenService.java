package com.exam.exam_backed.auth.service;

import com.exam.exam_backed.user.User;

import java.util.Optional;

public interface TokenService {
    String createToken(User user);

    Optional<User> validate(String token);

    void revoke(String token);
}
