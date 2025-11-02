package tech.cspioneer.backend.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.UUID;

@Component
@Slf4j
public class RequestLoggingFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        long start = System.currentTimeMillis();
        String rid = headerOrGenerate(request, "X-Request-Id");
        MDC.put("requestId", rid);
        response.setHeader("X-Request-Id", rid);
        try {
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - start;
            String user = currentUser();
            String ip = clientIp(request);
            String ua = headerOrBlank(request, "User-Agent");
            // One-line access-style log with useful context
            String authz = authorities();
            String details = detailsSummary();
            log.info("api request completed method={} path={} status={} duration={}ms userUuid={} authz=[{}] details={} ip={} ua=\"{}\" requestId={}",
                    request.getMethod(), request.getRequestURI(), response.getStatus(), duration, user, authz, details, ip, trim(ua, 200), rid);
            MDC.clear();
        }
    }

    private String headerOrGenerate(HttpServletRequest req, String name) {
        String v = req.getHeader(name);
        if (v == null || v.isBlank()) return UUID.randomUUID().toString();
        return v;
    }

    private String headerOrBlank(HttpServletRequest req, String name) {
        String v = req.getHeader(name);
        return v == null ? "" : v;
    }

    private String currentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth == null ? "anonymous" : String.valueOf(auth.getPrincipal());
    }

    private String authorities() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities() == null) return "";
        try {
            return auth.getAuthorities().stream().map(a -> a.getAuthority()).collect(java.util.stream.Collectors.joining(","));
        } catch (Exception e) { return ""; }
    }

    private String detailsSummary() {
        var a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null || a.getDetails() == null) return "";
        Object d = a.getDetails();
        try {
            if (d instanceof tech.cspioneer.backend.security.ApiKeyContext ctx) {
                return "ApiKeyContext{subject=" + ctx.getSubjectType() + ", orgUuid=" + ctx.getOrgUuid() + ", scopes=" + ctx.getScopes() + "}";
            }
        } catch (Exception ignore) {}
        return d.getClass().getSimpleName();
    }

    private String clientIp(HttpServletRequest req) {
        String ip = req.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isBlank()) return ip.split(",")[0].trim();
        ip = req.getHeader("X-Real-IP");
        if (ip != null && !ip.isBlank()) return ip;
        return req.getRemoteAddr();
    }

    private String trim(String s, int max) {
        if (s == null) return "";
        if (s.length() <= max) return s;
        return s.substring(0, max);
    }
}
