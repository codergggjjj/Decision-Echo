package com.exam.exam_backed.auth.service.impl;

import com.exam.exam_backed.auth.service.TokenService;
import com.exam.exam_backed.user.User;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class InMemoryTokenService implements TokenService {
    private static final long TOKEN_TTL_SECONDS = 24 * 60 * 60;
    private final Map<String, TokenSession> sessions = new ConcurrentHashMap<>();

    @Override
    public String createToken(User user) {
        String token = UUID.randomUUID().toString().replace("-", "");
        sessions.put(token, new TokenSession(user, Instant.now().plusSeconds(TOKEN_TTL_SECONDS)));
        return token;
    }

    @Override
    public Optional<User> validate(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        TokenSession session = sessions.get(token);
        if (session == null) {
            return Optional.empty();
        }
        if (session.expireAt().isBefore(Instant.now())) {
            sessions.remove(token);
            return Optional.empty();
        }
        return Optional.of(session.user());
    }

    @Override
    public void revoke(String token) {
        if (token != null) {
            sessions.remove(token);
        }
    }

    private record TokenSession(User user, Instant expireAt) {
    }
}
