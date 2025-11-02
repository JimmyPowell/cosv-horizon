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
import tech.cspioneer.backend.service.JwtService;
import tech.cspioneer.backend.service.TokenRevocationService;

import java.io.IOException;
import java.util.List;

@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final TokenRevocationService tokenRevocationService;

    public JwtAuthenticationFilter(JwtService jwtService, TokenRevocationService tokenRevocationService) {
        this.jwtService = jwtService;
        this.tokenRevocationService = tokenRevocationService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        String tokenPrefix = jwtService.getTokenPrefix().trim();
        if (header != null && header.startsWith(tokenPrefix)) {
            String token = header.substring(tokenPrefix.length()).trim();
            try {
                JwtService.TokenInfo info = jwtService.parseAccessToken(token);
                if (info != null && tokenRevocationService.isIssuedAfter(info.getSubjectUuid(), info.getIssuedAt())) {
                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                            info.getSubjectUuid(),
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + info.getRole()))
                    );
                    SecurityContextHolder.getContext().setAuthentication(auth);
                    log.debug("JWT authentication success subject={} role={} path={} requestId={}",
                            info.getSubjectUuid(), info.getRole(), request.getRequestURI(), MDC.get("requestId"));
                }
            } catch (Exception e) {
                // Invalid token, proceed without authentication
                log.warn("JWT authentication failed type={} path={} requestId={}",
                        e.getClass().getSimpleName(), request.getRequestURI(), MDC.get("requestId"));
            }
        }
        filterChain.doFilter(request, response);
    }
}
