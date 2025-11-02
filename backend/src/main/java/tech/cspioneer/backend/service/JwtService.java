package tech.cspioneer.backend.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {
    @Value("${security.jwt.secret}")
    private String secret;
    @Value("${security.jwt.issuer:cosv-horizon}")
    private String issuer;
    @Value("${security.jwt.access-ttl:PT15M}")
    private Duration accessTtl;
    @Value("${security.jwt.refresh-ttl:P30D}")
    private Duration refreshTtl;
    @Value("${security.jwt.token-prefix:Bearer }")
    private String tokenPrefix;

    private Algorithm algorithm() {
        return Algorithm.HMAC256(secret);
    }

    public String getTokenPrefix() {
        return tokenPrefix;
    }

    public String generateAccessToken(String userUuid, String role) {
        Instant now = Instant.now();
        return JWT.create()
                .withIssuer(issuer)
                .withSubject(userUuid)
                .withClaim("typ", "access")
                .withClaim("role", role)
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(now.plus(accessTtl)))
                .sign(algorithm());
    }

    public String generateRefreshToken(String userUuid, String role, String jti) {
        Instant now = Instant.now();
        String id = (jti != null && !jti.isBlank()) ? jti : UUID.randomUUID().toString();
        return JWT.create()
                .withIssuer(issuer)
                .withJWTId(id)
                .withSubject(userUuid)
                .withClaim("typ", "refresh")
                .withClaim("role", role)
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(now.plus(refreshTtl)))
                .sign(algorithm());
    }

    public TokenInfo parseAccessToken(String token) {
        DecodedJWT jwt = baseVerify(token);
        if (!"access".equals(jwt.getClaim("typ").asString())) {
            throw new IllegalArgumentException("invalid token type");
        }
        return toInfo(jwt);
    }

    public TokenInfo parseRefreshToken(String token) {
        DecodedJWT jwt = baseVerify(token);
        if (!"refresh".equals(jwt.getClaim("typ").asString())) {
            throw new IllegalArgumentException("invalid token type");
        }
        return toInfo(jwt);
    }

    private DecodedJWT baseVerify(String token) {
        JWTVerifier verifier = JWT.require(algorithm()).withIssuer(issuer).build();
        return verifier.verify(token);
    }

    public long ttlSecondsUntil(Instant expiresAt) {
        long seconds = expiresAt.getEpochSecond() - Instant.now().getEpochSecond();
        return Math.max(seconds, 0);
    }

    private TokenInfo toInfo(DecodedJWT jwt) {
        TokenInfo info = new TokenInfo();
        info.setJti(jwt.getId());
        info.setSubjectUuid(jwt.getSubject());
        info.setRole(jwt.getClaim("role").asString());
        info.setIssuedAt(jwt.getIssuedAt().toInstant());
        info.setExpiresAt(jwt.getExpiresAt().toInstant());
        return info;
    }

    public Duration getAccessTtl() { return accessTtl; }
    public Duration getRefreshTtl() { return refreshTtl; }

    public static class TokenInfo {
        private String jti;
        private String subjectUuid;
        private String role;
        private Instant issuedAt;
        private Instant expiresAt;

        public String getJti() { return jti; }
        public void setJti(String jti) { this.jti = jti; }
        public String getSubjectUuid() { return subjectUuid; }
        public void setSubjectUuid(String subjectUuid) { this.subjectUuid = subjectUuid; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public Instant getIssuedAt() { return issuedAt; }
        public void setIssuedAt(Instant issuedAt) { this.issuedAt = issuedAt; }
        public Instant getExpiresAt() { return expiresAt; }
        public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
    }
}

