package tech.cspioneer.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Locale;
import java.util.UUID;

@Service
public class PasswordResetCodeService {
    private final StringRedisTemplate redis;

    @Value("${auth.code.ttl:PT10M}")
    private Duration codeTtl;

    public PasswordResetCodeService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public static class CodeIssueResult {
        public final String requestId;
        public final String code;
        public CodeIssueResult(String requestId, String code) {
            this.requestId = requestId; this.code = code;
        }
    }

    public CodeIssueResult issueCode(String email) {
        String requestId = UUID.randomUUID().toString();
        String code = generateCode();
        String key = keyOf(email);
        String hash = hash(email, code, requestId);
        redis.opsForValue().set(key, requestId + ":" + hash, codeTtl);
        return new CodeIssueResult(requestId, code);
    }

    public boolean verifyAndConsume(String email, String code, String requestId) {
        String key = keyOf(email);
        String stored = redis.opsForValue().get(key);
        if (stored == null) return false;
        int idx = stored.indexOf(':');
        if (idx <= 0) return false;
        String storedReqId = stored.substring(0, idx);
        String storedHash = stored.substring(idx + 1);
        if (!storedReqId.equals(requestId)) return false;
        String calc = hash(email, code, requestId);
        boolean ok = storedHash.equals(calc);
        if (ok) redis.delete(key);
        return ok;
    }

    private String keyOf(String email) {
        return "pwd:code:" + email.toLowerCase(Locale.ROOT);
    }

    private String generateCode() {
        SecureRandom r = new SecureRandom();
        int n = r.nextInt(900000) + 100000; // 6 digits
        return String.valueOf(n);
    }

    private String hash(String email, String code, String requestId) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            String s = email.toLowerCase(Locale.ROOT) + "|" + requestId + "|" + code;
            byte[] dig = md.digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : dig) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public int ttlMinutes() {
        return (int) codeTtl.toMinutes();
    }
}

