package tech.cspioneer.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RateLimiterService {
    private final StringRedisTemplate redis;

    @Value("${auth.rate.email.per-minute:5}")
    private int emailPerMinute;
    @Value("${auth.rate.email.per-day:50}")
    private int emailPerDay;
    @Value("${auth.rate.ip.per-minute:10}")
    private int ipPerMinute;
    @Value("${auth.cooldown-seconds:60}")
    private int cooldownSeconds;

    public RateLimiterService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public boolean allowEmail(String email) {
        return incrWithin("rl:email:min:" + email, Duration.ofMinutes(1), emailPerMinute)
                && incrWithin("rl:email:day:" + email, Duration.ofDays(1), emailPerDay)
                && cooldown("rl:email:cool:" + email, Duration.ofSeconds(cooldownSeconds));
    }

    public boolean allowIp(String ip) {
        return incrWithin("rl:ip:min:" + ip, Duration.ofMinutes(1), ipPerMinute);
    }

    private boolean incrWithin(String key, Duration window, int limit) {
        Long v = redis.opsForValue().increment(key);
        if (v != null && v == 1L) {
            redis.expire(key, window);
        }
        return v != null && v <= limit;
    }

    private boolean cooldown(String key, Duration cool) {
        Boolean set = redis.opsForValue().setIfAbsent(key, "1", cool);
        return set != null && set;
    }
}

