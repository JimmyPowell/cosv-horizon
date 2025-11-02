package tech.cspioneer.backend.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class TokenBlacklistService {
    private final StringRedisTemplate redis;

    public TokenBlacklistService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public void blacklistRefreshJti(String jti, long ttlSeconds) {
        if (jti == null || jti.isBlank()) return;
        redis.opsForValue().set(keyOf(jti), "1", java.time.Duration.ofSeconds(Math.max(ttlSeconds, 0)));
    }

    public boolean isRefreshBlacklisted(String jti) {
        String v = redis.opsForValue().get(keyOf(jti));
        return v != null;
    }

    private String keyOf(String jti) { return "jwt:blacklist:" + jti; }
}
