package com.cosv.horizon.utils;

import com.cosv.horizon.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import javax.crypto.SecretKey;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    // 访问令牌过期时间: 5分钟
    private static final long ACCESS_TOKEN_EXPIRATION = 5 * 60;
    
    // 刷新令牌过期时间: 7天
    private static final long REFRESH_TOKEN_EXPIRATION = 7 * 24 * 60 * 60;
    
    // 刷新阈值: 12小时
    private static final long REFRESH_THRESHOLD = 12 * 60 * 60;
    
    // Redis中保存token版本的前缀
    private static final String TOKEN_VERSION_KEY_PREFIX = "token:version:";
    
    // 用于生成和验证JWT的密钥
    private Key signingKey;

    /**
     * 获取用于签名的密钥
     * @return 密钥
     */
    private Key getSigningKey() {
        if (signingKey == null) {
            try {
                // 尝试使用配置的密钥（如果足够长）
                if (secret != null && !secret.isEmpty()) {
                    try {
                        // 使用Base64解码配置的密钥
                        byte[] keyBytes = Base64.getDecoder().decode(secret);
                        if (keyBytes.length >= 32) { // 至少256位（32字节）
                            signingKey = Keys.hmacShaKeyFor(keyBytes);
                            return signingKey;
                        }
                    } catch (Exception e) {
                        // 解码失败，继续尝试下一种方法
                    }
                }
                
                // 如果配置的密钥不可用，生成新的安全密钥
                signingKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
            } catch (Exception e) {
                // 如果任何方法失败，使用最安全的方式生成密钥
                signingKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
            }
        }
        return signingKey;
    }

    /**
     * 生成访问令牌
     * @param username 用户名
     * @param userUuid 用户UUID
     * @return 访问令牌
     */
    public String generateAccessToken(String username, String userUuid) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userUuid", userUuid);
        claims.put("tokenType", "access");
        return createToken(claims, username, ACCESS_TOKEN_EXPIRATION);
    }
    
    /**
     * 生成刷新令牌
     * @param username 用户名
     * @param userUuid 用户UUID
     * @return 刷新令牌
     */
    public String generateRefreshToken(String username, String userUuid) {
        // 获取或创建token版本号
        Integer tokenVersion = getOrCreateTokenVersion(userUuid);
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("userUuid", userUuid);
        claims.put("tokenType", "refresh");
        claims.put("version", tokenVersion);
        return createToken(claims, username, REFRESH_TOKEN_EXPIRATION);
    }

    /**
     * 创建令牌
     * @param claims 声明
     * @param subject 主题
     * @param expirationSeconds 过期时间(秒)
     * @return 令牌
     */
    private String createToken(Map<String, Object> claims, String subject, long expirationSeconds) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationSeconds * 1000))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    
    /**
     * 验证令牌是否有效
     * @param token 令牌
     * @return 是否有效
     */
    public Boolean validateToken(String token) {
        try {
            // 检查令牌是否过期
            if (isTokenExpired(token)) {
                return false;
            }
            
            // 如果是刷新令牌，还需检查版本号
            if ("refresh".equals(extractTokenType(token))) {
                String userUuid = extractUserUuid(token);
                Integer tokenVersion = extractTokenVersion(token);
                Integer currentVersion = getCurrentTokenVersion(userUuid);
                
                // 版本号不匹配，令牌无效
                if (currentVersion == null || !tokenVersion.equals(currentVersion)) {
                    return false;
                }
            }
            
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 判断刷新令牌是否需要刷新
     * @param token 刷新令牌
     * @return 是否需要刷新
     */
    public boolean needsRefresh(String token) {
        try {
            Date expiration = extractExpiration(token);
            Date refreshDate = new Date(System.currentTimeMillis() + REFRESH_THRESHOLD * 1000);
            return expiration.before(refreshDate);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 更新刷新令牌的版本号
     * @param userUuid 用户UUID
     * @return 新的版本号
     */
    public Integer updateTokenVersion(String userUuid) {
        Integer version = getOrCreateTokenVersion(userUuid);
        version++;
        String key = TOKEN_VERSION_KEY_PREFIX + userUuid;
        RedisUtils.set(key, String.valueOf(version), 30 * 24 * 60 * 60, 0); // 30天有效期
        return version;
    }
    
    /**
     * 获取当前令牌版本号
     * @param userUuid 用户UUID
     * @return 当前版本号
     */
    private Integer getCurrentTokenVersion(String userUuid) {
        String key = TOKEN_VERSION_KEY_PREFIX + userUuid;
        String version = RedisUtils.get(key, 0);
        return version != null ? Integer.valueOf(version) : null;
    }
    
    /**
     * 获取或创建令牌版本号
     * @param userUuid 用户UUID
     * @return 版本号
     */
    private Integer getOrCreateTokenVersion(String userUuid) {
        Integer version = getCurrentTokenVersion(userUuid);
        if (version == null) {
            version = 1;
            String key = TOKEN_VERSION_KEY_PREFIX + userUuid;
            RedisUtils.set(key, String.valueOf(version), 30 * 24 * 60 * 60, 0); // 30天有效期
        }
        return version;
    }

    /**
     * 从令牌中提取用户名
     * @param token 令牌
     * @return 用户名
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * 从令牌中提取用户UUID
     * @param token 令牌
     * @return 用户UUID
     */
    public String extractUserUuid(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("userUuid").toString();
    }
    
    /**
     * 从令牌中提取令牌类型
     * @param token 令牌
     * @return 令牌类型
     */
    public String extractTokenType(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("tokenType").toString();
    }
    
    /**
     * 从令牌中提取版本号
     * @param token 令牌
     * @return 版本号
     */
    public Integer extractTokenVersion(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("version") != null ? Integer.valueOf(claims.get("version").toString()) : null;
    }

    /**
     * 从令牌中提取过期时间
     * @param token 令牌
     * @return 过期时间
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * 检查令牌是否过期
     * @param token 令牌
     * @return 是否过期
     */
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * 从令牌中提取声明
     * @param token 令牌
     * @param claimsResolver 声明解析器
     * @return 声明
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * 从令牌中提取所有声明
     * @param token 令牌
     * @return 声明
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    
    /**
     * 使当前用户的所有令牌失效
     * @param userUuid 用户UUID
     */
    public void invalidateUserTokens(String userUuid) {
        updateTokenVersion(userUuid);
    }
    
    /**
     * 为用户生成完整的令牌对（访问令牌和刷新令牌）
     * @param user 用户实体
     * @return 包含访问令牌和刷新令牌的Map
     */
    public Map<String, String> generateToken(User user) {
        String accessToken = generateAccessToken(user.getName(), user.getUuid());
        String refreshToken = generateRefreshToken(user.getName(), user.getUuid());
        
        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", accessToken);
        tokens.put("refreshToken", refreshToken);
        return tokens;
    }
} 