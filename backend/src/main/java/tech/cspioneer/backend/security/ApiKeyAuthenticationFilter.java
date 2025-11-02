package tech.cspioneer.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tech.cspioneer.backend.security.ApiKeyContext;
import tech.cspioneer.backend.service.ApiKeyService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private final ApiKeyService apiKeyService;

    public ApiKeyAuthenticationFilter(ApiKeyService apiKeyService) {
        this.apiKeyService = apiKeyService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            String header = request.getHeader("X-API-Key");
            if (header == null) header = request.getHeader(HttpHeaders.AUTHORIZATION);
            ApiKeyService.AuthContext ctx = apiKeyService.validateAndResolveContext(header, clientIp(request));
            if (ctx != null) {
                List<SimpleGrantedAuthority> auths = new ArrayList<>();
                auths.add(new SimpleGrantedAuthority("ROLE_API"));
                if (ctx.key.getScopes() != null) {
                    for (String s : ctx.key.getScopes().split(",")) {
                        String t = s.trim();
                        if (!t.isEmpty()) auths.add(new SimpleGrantedAuthority("SCOPE_" + t));
                    }
                }
                // 将用户UUID作为主体，并在details中附带组织上下文
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(ctx.userUuid, null, auths);
                ApiKeyContext details = new ApiKeyContext();
                details.setSubjectType(ctx.subjectType);
                details.setUserUuid(ctx.userUuid);
                details.setOrgUuid(ctx.orgUuid);
                details.setApiKeyUuid(ctx.key.getUuid());
                details.setScopes(ctx.key.getScopes());
                auth.setDetails(details);
                SecurityContextHolder.getContext().setAuthentication(auth);
                log.debug("API key authentication success subjectType={} userUuid={} orgUuid={} path={} requestId={}",
                        ctx.subjectType, ctx.userUuid, ctx.orgUuid, request.getRequestURI(), MDC.get("requestId"));
            } else if (header != null && !header.isBlank()) {
                log.warn("API key authentication failed path={} requestId={} (header present)",
                        request.getRequestURI(), MDC.get("requestId"));
            }
        }
        filterChain.doFilter(request, response);
    }

    private String clientIp(HttpServletRequest req) {
        String ip = req.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isBlank()) return ip.split(",")[0].trim();
        ip = req.getHeader("X-Real-IP");
        if (ip != null && !ip.isBlank()) return ip;
        return req.getRemoteAddr();
    }
}
