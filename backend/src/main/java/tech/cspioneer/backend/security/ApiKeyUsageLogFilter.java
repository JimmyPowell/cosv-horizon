package tech.cspioneer.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tech.cspioneer.backend.entity.ApiKey;
import tech.cspioneer.backend.entity.ApiKeyUsageLog;
import tech.cspioneer.backend.mapper.ApiKeyMapper;
import tech.cspioneer.backend.mapper.ApiKeyUsageLogMapper;

import java.io.IOException;
import java.util.UUID;

@Component
public class ApiKeyUsageLogFilter extends OncePerRequestFilter {
    private final ApiKeyUsageLogMapper logMapper;
    private final ApiKeyMapper apiKeyMapper;

    public ApiKeyUsageLogFilter(ApiKeyUsageLogMapper logMapper, ApiKeyMapper apiKeyMapper) {
        this.logMapper = logMapper;
        this.apiKeyMapper = apiKeyMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } finally {
            var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getDetails() instanceof ApiKeyContext ctx) {
                if (ctx.getApiKeyUuid() != null) {
                    ApiKey key = apiKeyMapper.findByUuid(ctx.getApiKeyUuid());
                    if (key != null) {
                        ApiKeyUsageLog log = new ApiKeyUsageLog();
                        log.setUuid(UUID.randomUUID().toString());
                        log.setApiKeyId(key.getId());
                        log.setRequestIpAddress(clientIp(request));
                        log.setRequestMethod(request.getMethod());
                        log.setRequestPath(request.getRequestURI());
                        log.setResponseStatusCode(response.getStatus());
                        String ua = request.getHeader("User-Agent");
                        log.setUserAgent(ua);
                        try { logMapper.insert(log); } catch (Exception ignored) {}
                    }
                }
            }
        }
    }

    private String clientIp(HttpServletRequest req) {
        String ip = req.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isBlank()) return ip.split(",")[0].trim();
        ip = req.getHeader("X-Real-IP");
        if (ip != null && !ip.isBlank()) return ip;
        return req.getRemoteAddr();
    }
}

