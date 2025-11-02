package tech.cspioneer.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Locale;
import java.util.UUID;

@Service
public class RegistrationSessionService {
    private final StringRedisTemplate redis;

    @Value("${auth.session.ttl:PT15M}")
    private Duration sessionTtl;

    public RegistrationSessionService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public String createSession(String email) {
        String token = UUID.randomUUID().toString();
        redis.opsForValue().set(keyOf(token), email.toLowerCase(Locale.ROOT), sessionTtl);
        return token;
    }

    public String consumeSession(String token) {
        String key = keyOf(token);
        String email = redis.opsForValue().get(key);
        if (email != null) {
            redis.delete(key);
        }
        return email;
    }

    private String keyOf(String token) { return "reg:session:" + token; }
}

