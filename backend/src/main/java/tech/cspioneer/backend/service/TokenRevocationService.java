package tech.cspioneer.backend.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class TokenRevocationService {
    private final StringRedisTemplate redis;

    public TokenRevocationService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public void revokeAll(String userUuid) {
        if (userUuid == null || userUuid.isBlank()) return;
        String key = keyOf(userUuid);
        String now = String.valueOf(Instant.now().getEpochSecond());
        redis.opsForValue().set(key, now);
    }

    public Instant getInvalidAfter(String userUuid) {
        String v = redis.opsForValue().get(keyOf(userUuid));
        if (v == null) return Instant.EPOCH; // no revocation set
        try {
            long epoch = Long.parseLong(v);
            return Instant.ofEpochSecond(epoch);
        } catch (NumberFormatException e) {
            return Instant.EPOCH;
        }
    }

    public boolean isIssuedAfter(String userUuid, Instant issuedAt) {
        if (issuedAt == null) return false;
        Instant invalidAfter = getInvalidAfter(userUuid);
        return issuedAt.isAfter(invalidAfter);
    }

    private String keyOf(String userUuid) { return "jwt:invalidAfter:" + userUuid; }
}

